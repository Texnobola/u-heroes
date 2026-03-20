package com.uheroes.mod.client.animation;

/**
 * Stub — old procedural attack animation replaced by JSON-based
 * KeyframeAnimationPlayer in SaberAttackAnimHandler.
 * Kept so SaberFirstPersonHandler compiles without changes.
 */
public class SaberAttackAnimation {
    public static final SaberAttackAnimation INSTANCE = new SaberAttackAnimation();
    private SaberAttackAnimation() {}

    public boolean isActive()      { return false; }
    public int     getPhase()      { return 0; }
    public float   getPhaseT()     { return 0f; }
    public int     getAttackType() { return 0; }
    public float   getFPYaw()      { return 0f; }
    public float   getFPPitch()    { return 0f; }
    public float   getFPRoll()     { return 0f; }
    public float   getFPTranslateZ() { return 0f; }
    public void    play(int type)  { }
    public void    tick()          { }
}