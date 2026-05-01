package com.example.android.net.dto;

/** DTO для бросков кубиков /rooms/{id}/dice/roll. */
public class DiceDtos {

    private DiceDtos() {}

    public static final class DiceKind {
        public static final String D4 = "d4";
        public static final String D6 = "d6";
        public static final String D8 = "d8";
        public static final String D10 = "d10";
        public static final String D12 = "d12";
        public static final String D20 = "d20";
        public static final String D100 = "d100";
        public static final String MAGIC_BALL = "MAGIC_BALL";
        private DiceKind() {}
    }

    public static final class DiceMode {
        public static final String PUBLIC = "PUBLIC";
        public static final String HIDDEN = "HIDDEN";
        private DiceMode() {}
    }

    public static class DiceRollRequest {
        public String dice;     // DiceKind
        public String mode;     // DiceMode
        public Integer modifier;

        public DiceRollRequest(String dice, String mode, Integer modifier) {
            this.dice = dice;
            this.mode = mode;
            this.modifier = modifier;
        }
    }

    public static class DiceRollResponse {
        public String rollId;
        public String dice;
        public Integer result;
        public String magicBallAnswer;
        public Integer modifier;
        public Integer total;
        public String mode;
        public String actorUserId;
        public String createdAt;
    }
}
