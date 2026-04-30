using FluentValidation;
using Rooms.Application.DTOs;
using Rooms.Entities;

namespace Rooms.Application.Validators
{
    public class CreateRoomRequestValidator : AbstractValidator<CreateRoomRequest>
    {
        public CreateRoomRequestValidator()
        {
            RuleFor(x => x.Name)
                .NotEmpty().WithMessage("Room name is required")
                .Must(n => !string.IsNullOrWhiteSpace(n)).WithMessage("Room name cannot be whitespace")
                .Length(1, 100).WithMessage("Room name must be 1 to 100 characters");

            RuleFor(x => x.AccessMode)
                .Must(m => m == AccessMode.PUBLIC || m == AccessMode.PRIVATE)
                .WithMessage("AccessMode must be PUBLIC or PRIVATE");
        }
    }
}
