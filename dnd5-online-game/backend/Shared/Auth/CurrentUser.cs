using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using Microsoft.AspNetCore.Http;
using Shared.Errors;

namespace Shared.Auth
{
    public class CurrentUser : ICurrentUser
    {
        private readonly IHttpContextAccessor _accessor;

        public CurrentUser(IHttpContextAccessor accessor)
        {
            _accessor = accessor;
        }

        private ClaimsPrincipal? Principal => _accessor.HttpContext?.User;

        public bool IsAuthenticated => Principal?.Identity?.IsAuthenticated == true;

        public Guid? UserId
        {
            get
            {
                var sub = Principal?.FindFirstValue(JwtRegisteredClaimNames.Sub)
                          ?? Principal?.FindFirstValue(ClaimTypes.NameIdentifier);
                return Guid.TryParse(sub, out var id) ? id : null;
            }
        }

        public string? Username =>
            Principal?.FindFirstValue(JwtRegisteredClaimNames.UniqueName)
            ?? Principal?.FindFirstValue(ClaimTypes.Name);

        public Guid RequireUserId()
        {
            return UserId ?? throw new UnauthorizedException("User is not authenticated");
        }
    }
}
