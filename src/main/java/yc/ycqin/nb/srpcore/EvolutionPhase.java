package yc.ycqin.nb.srpcore;

public enum EvolutionPhase {
    PHASE_MINUS_2(-2, "evolutionbar-2.png"),
    PHASE_MINUS_1(-1, "evolutionbar-1.png"),
    PHASE_0(0, "evolutionbar0.png"),
    PHASE_1(1, "evolutionbar1.png"),
    PHASE_2(2, "evolutionbar2.png"),
    PHASE_3(3, "evolutionbar3.png"),
    PHASE_4(4, "evolutionbar4.png"),
    PHASE_5(5, "evolutionbar5.png"),
    PHASE_6(6, "evolutionbar6.png"),
    PHASE_7(7, "evolutionbar7.png"),
    PHASE_8(8, "evolutionbar8.png"),
    PHASE_9(9, "evolutionbar9.png"),
    PHASE_10(10, "evolutionbar10.png");

    private final int phaseNum;
    private final String texturePath;

    EvolutionPhase(int phaseNum, String texturePath) {
        this.phaseNum = phaseNum;
        this.texturePath = "ycqin:textures/gui/" + texturePath;
    }

    public int getPhaseNum() {
        return phaseNum;
    }

    public String getTexturePath() {
        return texturePath;
    }

    // 根据数值获取对应阶段
    public static EvolutionPhase getByNumber(int phaseNum) {
        for (EvolutionPhase phase : values()) {
            if (phase.phaseNum == phaseNum) {
                return phase;
            }
        }
        return PHASE_0; // 默认阶段0
    }
}
