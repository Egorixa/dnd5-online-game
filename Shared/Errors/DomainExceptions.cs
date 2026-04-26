namespace Shared.Errors
{
    public class ValidationException : Exception
    {
        public List<ApiErrorDetail> Details { get; }

        public ValidationException(string message, List<ApiErrorDetail>? details = null)
            : base(message)
        {
            Details = details ?? new List<ApiErrorDetail>();
        }

        public ValidationException(string field, string message)
            : base("Validation failed")
        {
            Details = new List<ApiErrorDetail>
            {
                new() { Field = field, Message = message }
            };
        }
    }

    public class NotFoundException : Exception
    {
        public NotFoundException(string message) : base(message) { }
    }

    public class ForbiddenException : Exception
    {
        public ForbiddenException(string message) : base(message) { }
    }

    public class ConflictException : Exception
    {
        public string Code { get; }

        public ConflictException(string code, string message) : base(message)
        {
            Code = code;
        }
    }

    public class UnauthorizedException : Exception
    {
        public UnauthorizedException(string message = "Unauthorized") : base(message) { }
    }
}
