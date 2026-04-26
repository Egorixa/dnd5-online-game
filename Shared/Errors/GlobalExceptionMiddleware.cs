using System.Text.Json;
using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.Logging;

namespace Shared.Errors
{
    public class GlobalExceptionMiddleware
    {
        private readonly RequestDelegate _next;
        private readonly ILogger<GlobalExceptionMiddleware> _logger;

        private static readonly JsonSerializerOptions JsonOptions = new()
        {
            PropertyNamingPolicy = JsonNamingPolicy.CamelCase
        };

        public GlobalExceptionMiddleware(RequestDelegate next, ILogger<GlobalExceptionMiddleware> logger)
        {
            _next = next;
            _logger = logger;
        }

        public async Task InvokeAsync(HttpContext context)
        {
            try
            {
                await _next(context);
            }
            catch (ValidationException ex)
            {
                await WriteError(context, StatusCodes.Status400BadRequest,
                    new ApiError { Code = "VALIDATION_ERROR", Message = ex.Message, Details = ex.Details });
            }
            catch (UnauthorizedException ex)
            {
                await WriteError(context, StatusCodes.Status401Unauthorized,
                    new ApiError { Code = "UNAUTHORIZED", Message = ex.Message });
            }
            catch (ForbiddenException ex)
            {
                await WriteError(context, StatusCodes.Status403Forbidden,
                    new ApiError { Code = "FORBIDDEN", Message = ex.Message });
            }
            catch (NotFoundException ex)
            {
                await WriteError(context, StatusCodes.Status404NotFound,
                    new ApiError { Code = "NOT_FOUND", Message = ex.Message });
            }
            catch (ConflictException ex)
            {
                await WriteError(context, StatusCodes.Status409Conflict,
                    new ApiError { Code = ex.Code, Message = ex.Message });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Unhandled exception");
                await WriteError(context, StatusCodes.Status500InternalServerError,
                    new ApiError { Code = "INTERNAL_ERROR", Message = "Internal server error" });
            }
        }

        private static Task WriteError(HttpContext context, int statusCode, ApiError error)
        {
            context.Response.StatusCode = statusCode;
            context.Response.ContentType = "application/json; charset=utf-8";
            return context.Response.WriteAsync(JsonSerializer.Serialize(error, JsonOptions));
        }
    }
}
