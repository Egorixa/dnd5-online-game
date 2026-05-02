using FluentValidation;
using Identity.Application.DTOs;

namespace Identity.Application.Validators
{
    public class RegisterRequestValidator : AbstractValidator<RegisterRequest>
    {
        public RegisterRequestValidator()
        {
            RuleFor(x => x.Username)
                .NotEmpty().WithMessage("Username is required")
                .Length(3, 20).WithMessage("Username must be 3 to 20 characters")
                .Matches("^[a-zA-Z0-9_]+$").WithMessage("Username may contain only letters, digits and underscore");

            RuleFor(x => x.Password)
                .NotEmpty().WithMessage("Password is required")
                .Length(6, 30).WithMessage("Password must be 6 to 30 characters");
        }
    }

    public class LoginRequestValidator : AbstractValidator<LoginRequest>
    {
        public LoginRequestValidator()
        {
            RuleFor(x => x.Username).NotEmpty();
            RuleFor(x => x.Password).NotEmpty();
        }
    }
}
