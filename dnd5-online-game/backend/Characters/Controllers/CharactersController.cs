using Characters.Application.DTOs;
using Characters.Application.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Shared.Auth;

namespace Characters.Controllers
{
    [ApiController]
    [Authorize]
    [Route("rooms/{roomId:guid}/characters")]
    public class CharactersController : ControllerBase
    {
        private readonly ICharacterService _service;
        private readonly ICurrentUser _currentUser;

        public CharactersController(ICharacterService service, ICurrentUser currentUser)
        {
            _service = service;
            _currentUser = currentUser;
        }

        [HttpPost]
        public async Task<IActionResult> Create(Guid roomId, [FromBody] CharacterUpsertRequest request, CancellationToken ct)
        {
            var userId = _currentUser.RequireUserId();
            var response = await _service.CreateAsync(userId, roomId, request, ct);
            return StatusCode(StatusCodes.Status201Created, response);
        }

        [HttpGet]
        public async Task<IActionResult> List(Guid roomId, CancellationToken ct)
        {
            var userId = _currentUser.RequireUserId();
            var characters = await _service.ListByRoomAsync(userId, roomId, ct);
            return Ok(new { characters });
        }

        [HttpGet("{characterId:guid}")]
        public async Task<IActionResult> Get(Guid roomId, Guid characterId, CancellationToken ct)
        {
            var userId = _currentUser.RequireUserId();
            var response = await _service.GetAsync(userId, roomId, characterId, ct);
            return Ok(response);
        }

        [HttpPatch("{characterId:guid}")]
        public async Task<IActionResult> Update(Guid roomId, Guid characterId, [FromBody] CharacterUpsertRequest request, CancellationToken ct)
        {
            var userId = _currentUser.RequireUserId();
            var response = await _service.UpdateAsync(userId, roomId, characterId, request, ct);
            return Ok(response);
        }

        [HttpDelete("{characterId:guid}")]
        public async Task<IActionResult> Delete(Guid roomId, Guid characterId, CancellationToken ct)
        {
            var userId = _currentUser.RequireUserId();
            await _service.DeleteAsync(userId, roomId, characterId, ct);
            return NoContent();
        }

        [HttpPost("{characterId:guid}/attacks")]
        public async Task<IActionResult> AddAttack(Guid roomId, Guid characterId, [FromBody] AttackDto dto, CancellationToken ct)
        {
            var userId = _currentUser.RequireUserId();
            var result = await _service.AddAttackAsync(userId, roomId, characterId, dto, ct);
            return StatusCode(StatusCodes.Status201Created, result);
        }

        [HttpPut("{characterId:guid}/attacks/{attackId:guid}")]
        public async Task<IActionResult> UpdateAttack(Guid roomId, Guid characterId, Guid attackId, [FromBody] AttackDto dto, CancellationToken ct)
        {
            var userId = _currentUser.RequireUserId();
            var result = await _service.UpdateAttackAsync(userId, roomId, characterId, attackId, dto, ct);
            return Ok(result);
        }

        [HttpDelete("{characterId:guid}/attacks/{attackId:guid}")]
        public async Task<IActionResult> DeleteAttack(Guid roomId, Guid characterId, Guid attackId, CancellationToken ct)
        {
            var userId = _currentUser.RequireUserId();
            await _service.DeleteAttackAsync(userId, roomId, characterId, attackId, ct);
            return NoContent();
        }

        [HttpPost("{characterId:guid}/spells")]
        public async Task<IActionResult> AddSpell(Guid roomId, Guid characterId, [FromBody] SpellDto dto, CancellationToken ct)
        {
            var userId = _currentUser.RequireUserId();
            var result = await _service.AddSpellAsync(userId, roomId, characterId, dto, ct);
            return StatusCode(StatusCodes.Status201Created, result);
        }

        [HttpPut("{characterId:guid}/spells/{spellId:guid}")]
        public async Task<IActionResult> UpdateSpell(Guid roomId, Guid characterId, Guid spellId, [FromBody] SpellDto dto, CancellationToken ct)
        {
            var userId = _currentUser.RequireUserId();
            var result = await _service.UpdateSpellAsync(userId, roomId, characterId, spellId, dto, ct);
            return Ok(result);
        }

        [HttpDelete("{characterId:guid}/spells/{spellId:guid}")]
        public async Task<IActionResult> DeleteSpell(Guid roomId, Guid characterId, Guid spellId, CancellationToken ct)
        {
            var userId = _currentUser.RequireUserId();
            await _service.DeleteSpellAsync(userId, roomId, characterId, spellId, ct);
            return NoContent();
        }
    }
}
