namespace Identity.Application.Helpers
{
    public static class PasswordHasher
    {
        private const int WorkFactor = 11;

        public static string HashPassword(string password)
            => BCrypt.Net.BCrypt.HashPassword(password, WorkFactor);

        public static bool VerifyPassword(string inputPassword, string storedHash)
        {
            try
            {
                return BCrypt.Net.BCrypt.Verify(inputPassword, storedHash);
            }
            catch (BCrypt.Net.SaltParseException)
            {
                return false;
            }
        }
    }
}
