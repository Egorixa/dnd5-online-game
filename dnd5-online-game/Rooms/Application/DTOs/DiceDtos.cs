namespace Rooms.Application.DTOs
{
    public enum DiceKind
    {
        d4,
        d6,
        d8,
        d10,
        d12,
        d20,
        d100,
        MAGIC_BALL
    }

    public enum DiceMode
    {
        PUBLIC,
        HIDDEN
    }

    public class DiceRollRequest
    {
        public DiceKind Dice { get; set; }
        public DiceMode Mode { get; set; } = DiceMode.PUBLIC;
        public int? Modifier { get; set; }
    }

    public class DiceRollResponse
    {
        public Guid RollId { get; set; }
        public DiceKind Dice { get; set; }
        public int? Result { get; set; }
        public string? MagicBallAnswer { get; set; }
        public int? Modifier { get; set; }
        public int? Total { get; set; }
        public DiceMode Mode { get; set; }
        public Guid ActorUserId { get; set; }
        public DateTime CreatedAt { get; set; }
    }
}
