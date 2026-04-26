using System.Security.Cryptography;
using System.Text.Json;
using Microsoft.EntityFrameworkCore;
using Rooms.Application.DTOs;
using Rooms.Application.Interfaces;
using Rooms.Data;
using Rooms.Entities;
using Shared.Errors;
using Shared.RealTime;

namespace Rooms.Application.Services
{
    public interface IDiceService
    {
        Task<DiceRollResponse> RollAsync(Guid userId, Guid roomId, DiceRollRequest request, CancellationToken ct = default);
    }

    public class DiceService : IDiceService
    {
        private static readonly string[] MagicBallAnswers = { "Да", "Нет", "Возможно" };

        private readonly RoomsDbContext _context;
        private readonly IRoomAccessChecker _access;
        private readonly IRoomNotifier _notifier;

        public DiceService(RoomsDbContext context, IRoomAccessChecker access, IRoomNotifier notifier)
        {
            _context = context;
            _access = access;
            _notifier = notifier;
        }

        public async Task<DiceRollResponse> RollAsync(Guid userId, Guid roomId, DiceRollRequest request, CancellationToken ct = default)
        {
            var info = await _access.RequireParticipantAsync(userId, roomId, ct);

            if (info.Room.Status == RoomStatus.Finished)
                throw new ForbiddenException("Cannot roll dice in a finished room");

            var response = new DiceRollResponse
            {
                RollId = Guid.NewGuid(),
                Dice = request.Dice,
                Mode = request.Mode,
                Modifier = request.Modifier,
                ActorUserId = userId,
                CreatedAt = DateTime.UtcNow
            };

            if (request.Dice == DiceKind.MAGIC_BALL)
            {
                response.MagicBallAnswer = MagicBallAnswers[RandomNumberGenerator.GetInt32(MagicBallAnswers.Length)];
            }
            else
            {
                int faces = DiceFaces(request.Dice);
                response.Result = RandomNumberGenerator.GetInt32(1, faces + 1);
                response.Total = response.Result + (request.Modifier ?? 0);
            }

            _context.Events.Add(new RoomEvent
            {
                EventId = response.RollId,
                RoomId = roomId,
                Type = RoomEventType.DiceRolled,
                ActorUserId = userId,
                Payload = JsonSerializer.Serialize(response),
                CreatedAt = response.CreatedAt
            });

            await _context.SaveChangesAsync(ct);

            if (request.Mode == DiceMode.PUBLIC)
            {
                await _notifier.NotifyAsync(roomId, HubEvents.DiceRolled, response, ct);
            }
            else
            {
                // HIDDEN: notify only the master
                await _notifier.NotifyUserAsync(info.Room.MasterId, HubEvents.DiceRolled, response, ct);
                if (info.Room.MasterId != userId)
                    await _notifier.NotifyUserAsync(userId, HubEvents.DiceRolled, response, ct);
            }

            return response;
        }

        private static int DiceFaces(DiceKind kind) => kind switch
        {
            DiceKind.d4 => 4,
            DiceKind.d6 => 6,
            DiceKind.d8 => 8,
            DiceKind.d10 => 10,
            DiceKind.d12 => 12,
            DiceKind.d20 => 20,
            DiceKind.d100 => 100,
            _ => throw new ValidationException("dice", $"Unsupported dice kind {kind}")
        };
    }
}
