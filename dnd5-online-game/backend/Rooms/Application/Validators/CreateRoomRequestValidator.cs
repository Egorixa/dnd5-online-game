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
                .MaximumLength(100).WithMessage("Room name must be at most 100 characters");

            RuleFor(x => x.AccessMode)
                .Must(m => m == AccessMode.PUBLIC || m == AccessMode.PRIVATE)
                .WithMessage("AccessMode must be PUBLIC or PRIVATE");
        }
    }
}
