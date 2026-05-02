using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Rooms.Application.DTOs;
using Rooms.Application.Services;
using Shared.Auth;

namespace Rooms.Controllers
{
    [ApiController]
    [Authorize]
    [Route("rooms/{roomId:guid}/dice")]
    public class DiceController : ControllerBase
    {
        private readonly IDiceService _service;
        private readonly ICurrentUser _currentUser;

        public DiceController(IDiceService service, ICurrentUser currentUser)
        {
            _service = service;
            _currentUser = currentUser;
        }

        [HttpPost("roll")]
        public async Task<IActionResult> Roll(Guid roomId, [FromBody] DiceRollRequest request, CancellationToken ct)
        {
            var userId = _currentUser.RequireUserId();
            var response = await _service.RollAsync(userId, roomId, request, ct);
            return Ok(response);
        }
    }
}
