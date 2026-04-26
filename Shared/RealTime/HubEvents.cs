namespace Shared.RealTime
{
    public static class HubEvents
    {
        public const string RoomUpdated = "room.updated";
        public const string CharacterUpdated = "character.updated";
        public const string DiceRolled = "dice.rolled";
        public const string ParticipantJoined = "participant.joined";
        public const string ParticipantLeft = "participant.left";
    }

    public static class HubMethods
    {
        public const string JoinRoom = "JoinRoom";
        public const string LeaveRoom = "LeaveRoom";
    }
}
