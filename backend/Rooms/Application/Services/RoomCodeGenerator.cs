using System.Security.Cryptography;

namespace Rooms.Application.Services
{
    public static class RoomCodeGenerator
    {
        // Excludes visually similar characters (0/O, 1/I/L)
        private const string Alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        private const int Length = 6;

        public static string Generate()
        {
            Span<byte> buffer = stackalloc byte[Length];
            RandomNumberGenerator.Fill(buffer);
            Span<char> chars = stackalloc char[Length];
            for (int i = 0; i < Length; i++)
            {
                chars[i] = Alphabet[buffer[i] % Alphabet.Length];
            }
            return new string(chars);
        }
    }
}
