using Characters.Application.DTOs;
using Characters.Application.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Shared.Auth;

namespace Characters.Controllers
{
    /// <summary>
    /// Character templates that live in the user's account, independent of any session.
    /// See command TZ 4.1.1.3.2.4 ("Просмотр списка шаблонов листов персонажей")
    /// and private TZ 6.4 п.6 ("Сохранение шаблона листа персонажа").
    /// </summary>
    [ApiController]
    [Authorize]
    [Route("characters")]
    public class CharacterTemplatesController : ControllerBase
    {
        private readonly ICharacterService _service;
        private readonly ICurrentUser _currentUser;

        public CharacterTemplatesController(ICharacterService service, ICurrentUser currentUser)
        {
            _service = service;
            _currentUser = currentUser;
        }

        [HttpGet]
        public async Task<IActionResult> List(CancellationToken ct)
        {
            var userId = _currentUser.RequireUserId();
            var characters = await _service.ListTemplatesAsync(userId, ct);
            return Ok(new { characters });
        }

        [HttpPost]
        public async Task<IActionResult> Create([FromBody] CharacterUpsertRequest request, CancellationToken ct)
        {
            var userId = _currentUser.RequireUserId();
            var response = await _service.CreateTemplateAsync(userId, request, ct);
            return StatusCode(StatusCodes.Status201Created, response);
        }

        [HttpGet("{characterId:guid}")]
        public async Task<IActionResult> Get(Guid characterId, CancellationToken ct)
        {
            var userId = _currentUser.RequireUserId();
            var response = await _service.GetTemplateAsync(userId, characterId, ct);
            return Ok(response);
        }

        [HttpPatch("{characterId:guid}")]
        public async Task<IActionResult> Update(Guid characterId, [FromBody] CharacterUpsertRequest request, CancellationToken ct)
        {
            var userId = _currentUser.RequireUserId();
            var response = await _service.UpdateTemplateAsync(userId, characterId, request, ct);
            return Ok(response);
        }

        [HttpDelete("{characterId:guid}")]
        public async Task<IActionResult> Delete(Guid characterId, CancellationToken ct)
        {
            var userId = _currentUser.RequireUserId();
            await _service.DeleteTemplateAsync(userId, characterId, ct);
            return NoContent();
        }
    }
}
