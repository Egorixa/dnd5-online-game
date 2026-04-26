using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Rooms.Application.DTOs;
using Rooms.Application.Interfaces;
using Shared.Auth;

namespace Rooms.Controllers
{
    [ApiController]
    [Authorize]
    [Route("rooms")]
    public class RoomsController : ControllerBase
    {
        private readonly IRoomService _service;
        private readonly ICurrentUser _currentUser;

        public RoomsController(IRoomService service, ICurrentUser currentUser)
        {
            _service = service;
            _currentUser = currentUser;
        }

        [HttpPost]
        public async Task<IActionResult> Create([FromBody] CreateRoomRequest request, CancellationToken ct)
        {
            var userId = _currentUser.RequireUserId();
            var response = await _service.CreateAsync(userId, request, ct);
            return StatusCode(StatusCodes.Status201Created, response);
        }

        [HttpGet("public")]
        public async Task<IActionResult> GetPublic([FromQuery] int limit = 20, [FromQuery] int offset = 0, CancellationToken ct = default)
        {
            _currentUser.RequireUserId();
            var rooms = await _service.GetPublicAsync(limit, offset, ct);
            return Ok(new { rooms });
        }

        [HttpPost("{roomCode}/join")]
        public async Task<IActionResult> Join(string roomCode, CancellationToken ct)
        {
            var userId = _currentUser.RequireUserId();
            var response = await _service.JoinAsync(userId, roomCode, ct);
            return Ok(response);
        }

        [HttpPost("{roomId:guid}/leave")]
        public async Task<IActionResult> Leave(Guid roomId, CancellationToken ct)
        {
            var userId = _currentUser.RequireUserId();
            await _service.LeaveAsync(userId, roomId, ct);
            return NoContent();
        }

        [HttpPost("{roomId:guid}/kick")]
        public async Task<IActionResult> Kick(Guid roomId, [FromBody] KickParticipantRequest request, CancellationToken ct)
        {
            var userId = _currentUser.RequireUserId();
            await _service.KickAsync(userId, roomId, request, ct);
            return NoContent();
        }

        [HttpPost("{roomId:guid}/finish")]
        public async Task<IActionResult> Finish(Guid roomId, [FromBody] FinishRoomRequest request, CancellationToken ct)
        {
            var userId = _currentUser.RequireUserId();
            await _service.FinishAsync(userId, roomId, request, ct);
            return NoContent();
        }

        [HttpGet("{roomId:guid}/events")]
        public async Task<IActionResult> GetEvents(Guid roomId, [FromQuery] int limit = 50, [FromQuery] int offset = 0, CancellationToken ct = default)
        {
            var userId = _currentUser.RequireUserId();
            var events = await _service.GetEventsAsync(userId, roomId, limit, offset, ct);
            return Ok(new { events });
        }

        [HttpGet("{roomId:guid}/state")]
        public async Task<IActionResult> GetState(Guid roomId, CancellationToken ct)
        {
            var userId = _currentUser.RequireUserId();
            var state = await _service.GetStateAsync(userId, roomId, ct);
            return Ok(state);
        }
    }
}
