using System.Text.Json;
using Identity.Application.Interfaces;
using Microsoft.EntityFrameworkCore;
using Rooms.Application.DTOs;
using Rooms.Application.Interfaces;
using Rooms.Data;
using Rooms.Entities;
using Shared.Errors;
using Shared.RealTime;

namespace Rooms.Application.Services
{
    public class RoomService : IRoomService
    {
        private readonly RoomsDbContext _context;
        private readonly IRoomAccessChecker _access;
        private readonly IUserStatsService _stats;
        private readonly IRoomNotifier _notifier;

        public RoomService(
            RoomsDbContext context,
            IRoomAccessChecker access,
            IUserStatsService stats,
            IRoomNotifier notifier)
        {
            _context = context;
            _access = access;
            _stats = stats;
            _notifier = notifier;
        }

        public async Task<CreateRoomResponse> CreateAsync(Guid userId, CreateRoomRequest request, CancellationToken ct = default)
        {
            string code;
            int attempts = 0;
            while (true)
            {
                code = RoomCodeGenerator.Generate();
                if (!await _context.Rooms.AnyAsync(r => r.RoomCode == code, ct))
                    break;
                if (++attempts > 10)
                    throw new ConflictException("ROOM_CODE_CONFLICT", "Could not allocate a unique room code");
            }

            var room = new Room
            {
                RoomId = Guid.NewGuid(),
                RoomCode = code,
                MasterId = userId,
                AccessMode = request.AccessMode,
                Status = RoomStatus.Active,
                CreatedAt = DateTime.UtcNow
            };

            var participant = new RoomParticipant
            {
                ParticipantId = Guid.NewGuid(),
                RoomId = room.RoomId,
                UserId = userId,
                Role = ParticipantRole.Master,
                JoinedAt = DateTime.UtcNow
            };

            _context.Rooms.Add(room);
            _context.Participants.Add(participant);
            await _context.SaveChangesAsync(ct);

            return new CreateRoomResponse
            {
                RoomId = room.RoomId,
                RoomCode = room.RoomCode,
                AccessMode = room.AccessMode,
                CreatedAt = room.CreatedAt
            };
        }

        public async Task<List<PublicRoomDto>> GetPublicAsync(int limit, int offset, CancellationToken ct = default)
        {
            limit = Math.Clamp(limit, 1, 100);
            offset = Math.Max(0, offset);

            return await _context.Rooms
                .Where(r => r.AccessMode == AccessMode.PUBLIC && r.Status != RoomStatus.Finished)
                .OrderByDescending(r => r.CreatedAt)
                .Skip(offset).Take(limit)
                .Select(r => new PublicRoomDto
                {
                    RoomId = r.RoomId,
                    RoomCode = r.RoomCode,
                    MasterId = r.MasterId,
                    PlayersCount = r.Participants.Count(p => p.LeftAt == null),
                    CreatedAt = r.CreatedAt
                })
                .ToListAsync(ct);
        }

        public async Task<JoinRoomResponse> JoinAsync(Guid userId, string roomCode, CancellationToken ct = default)
        {
            var room = await _context.Rooms
                .Include(r => r.Participants)
                .FirstOrDefaultAsync(r => r.RoomCode == roomCode, ct)
                ?? throw new NotFoundException("Room not found");

            if (room.Status == RoomStatus.Finished)
                throw new ForbiddenException("Room is finished");

            var existingActive = room.Participants
                .FirstOrDefault(p => p.UserId == userId && p.LeftAt == null);
            if (existingActive != null)
                throw new ConflictException("ALREADY_JOINED", "User is already in the room");

            var participant = new RoomParticipant
            {
                ParticipantId = Guid.NewGuid(),
                RoomId = room.RoomId,
                UserId = userId,
                Role = room.MasterId == userId ? ParticipantRole.Master : ParticipantRole.Player,
                JoinedAt = DateTime.UtcNow
            };

            _context.Participants.Add(participant);

            // Resume room if master rejoins a paused session
            if (room.Status == RoomStatus.Paused && room.MasterId == userId)
                room.Status = RoomStatus.Active;

            AppendEvent(room.RoomId, RoomEventType.ParticipantJoined, userId, new
            {
                participantId = participant.ParticipantId,
                userId,
                role = participant.Role.ToString()
            });

            await _context.SaveChangesAsync(ct);

            await _notifier.NotifyAsync(room.RoomId, HubEvents.ParticipantJoined, new
            {
                participantId = participant.ParticipantId,
                userId,
                role = participant.Role.ToString(),
                joinedAt = participant.JoinedAt
            }, ct);

            var state = await BuildStateAsync(room.RoomId, ct);

            return new JoinRoomResponse
            {
                RoomId = room.RoomId,
                RoomCode = room.RoomCode,
                AccessMode = room.AccessMode,
                ParticipantId = participant.ParticipantId,
                Role = participant.Role,
                CurrentState = state
            };
        }

        public async Task LeaveAsync(Guid userId, Guid roomId, CancellationToken ct = default)
        {
            var info = await _access.RequireParticipantAsync(userId, roomId, ct);

            info.Participant.LeftAt = DateTime.UtcNow;

            // If the master leaves an active room — pause the session
            if (info.IsMaster && info.Room.Status == RoomStatus.Active)
            {
                info.Room.Status = RoomStatus.Paused;
                AppendEvent(roomId, RoomEventType.RoomPaused, userId, new { reason = "master_left" });
            }

            AppendEvent(roomId, RoomEventType.ParticipantLeft, userId, new
            {
                participantId = info.Participant.ParticipantId,
                userId
            });

            await _context.SaveChangesAsync(ct);

            await _notifier.NotifyAsync(roomId, HubEvents.ParticipantLeft, new
            {
                participantId = info.Participant.ParticipantId,
                userId,
                wasMaster = info.IsMaster
            }, ct);

            if (info.IsMaster)
            {
                await _notifier.NotifyAsync(roomId, HubEvents.RoomUpdated, new
                {
                    status = RoomStatus.Paused.ToString()
                }, ct);
            }
        }

        public async Task KickAsync(Guid masterUserId, Guid roomId, KickParticipantRequest request, CancellationToken ct = default)
        {
            var info = await _access.RequireMasterAsync(masterUserId, roomId, ct);

            RoomParticipant? target = null;
            if (request.TargetParticipantId.HasValue)
            {
                target = info.Room.Participants.FirstOrDefault(p =>
                    p.ParticipantId == request.TargetParticipantId.Value && p.LeftAt == null);
            }
            else if (request.TargetUserId.HasValue)
            {
                target = info.Room.Participants.FirstOrDefault(p =>
                    p.UserId == request.TargetUserId.Value && p.LeftAt == null);
            }
            else
            {
                throw new ValidationException("target", "Either targetUserId or targetParticipantId is required");
            }

            if (target is null)
                throw new NotFoundException("Participant not found");

            if (target.UserId == masterUserId)
                throw new ValidationException("target", "Master cannot kick themselves");

            target.LeftAt = DateTime.UtcNow;

            AppendEvent(roomId, RoomEventType.ParticipantKicked, masterUserId, new
            {
                participantId = target.ParticipantId,
                userId = target.UserId
            });

            await _context.SaveChangesAsync(ct);

            await _notifier.NotifyAsync(roomId, HubEvents.ParticipantLeft, new
            {
                participantId = target.ParticipantId,
                userId = target.UserId,
                kicked = true
            }, ct);
        }

        public async Task FinishAsync(Guid masterUserId, Guid roomId, FinishRoomRequest request, CancellationToken ct = default)
        {
            var info = await _access.RequireMasterAsync(masterUserId, roomId, ct);

            if (info.Room.Status == RoomStatus.Finished)
                throw new ConflictException("ALREADY_FINISHED", "Room is already finished");

            info.Room.Status = RoomStatus.Finished;
            info.Room.FinishedAt = DateTime.UtcNow;

            // mark all active participants as left
            foreach (var p in info.Room.Participants.Where(p => p.LeftAt == null))
            {
                p.LeftAt = info.Room.FinishedAt;
            }

            await _stats.IncrementWinsAsync(request.Winners, ct);
            await _stats.IncrementDefeatsAsync(request.Losers, ct);
            await _stats.IncrementMasterCountAsync(masterUserId, ct);

            AppendEvent(roomId, RoomEventType.RoomFinished, masterUserId, new
            {
                winners = request.Winners,
                losers = request.Losers
            });

            await _context.SaveChangesAsync(ct);

            await _notifier.NotifyAsync(roomId, HubEvents.RoomUpdated, new
            {
                status = RoomStatus.Finished.ToString(),
                finishedAt = info.Room.FinishedAt
            }, ct);
        }

        public async Task<List<RoomEventDto>> GetEventsAsync(Guid userId, Guid roomId, int limit, int offset, CancellationToken ct = default)
        {
            await _access.RequireParticipantAsync(userId, roomId, ct);

            limit = Math.Clamp(limit, 1, 200);
            offset = Math.Max(0, offset);

            return await _context.Events
                .Where(e => e.RoomId == roomId)
                .OrderByDescending(e => e.CreatedAt)
                .Skip(offset).Take(limit)
                .Select(e => new RoomEventDto
                {
                    EventId = e.EventId,
                    Type = e.Type,
                    ActorUserId = e.ActorUserId,
                    Payload = e.Payload,
                    CreatedAt = e.CreatedAt
                })
                .ToListAsync(ct);
        }

        public async Task<RoomStateDto> GetStateAsync(Guid userId, Guid roomId, CancellationToken ct = default)
        {
            await _access.RequireParticipantAsync(userId, roomId, ct);
            return await BuildStateAsync(roomId, ct);
        }

        private async Task<RoomStateDto> BuildStateAsync(Guid roomId, CancellationToken ct)
        {
            var room = await _context.Rooms
                .Include(r => r.Participants)
                .FirstOrDefaultAsync(r => r.RoomId == roomId, ct)
                ?? throw new NotFoundException("Room not found");

            return new RoomStateDto
            {
                RoomId = room.RoomId,
                Status = room.Status,
                MasterId = room.MasterId,
                Participants = room.Participants
                    .Where(p => p.LeftAt == null)
                    .Select(p => new RoomParticipantDto
                    {
                        ParticipantId = p.ParticipantId,
                        UserId = p.UserId,
                        Role = p.Role,
                        JoinedAt = p.JoinedAt
                    })
                    .ToList()
            };
        }

        private void AppendEvent(Guid roomId, RoomEventType type, Guid? actorUserId, object payload)
        {
            _context.Events.Add(new RoomEvent
            {
                EventId = Guid.NewGuid(),
                RoomId = roomId,
                Type = type,
                ActorUserId = actorUserId,
                Payload = JsonSerializer.Serialize(payload),
                CreatedAt = DateTime.UtcNow
            });
        }
    }
}
