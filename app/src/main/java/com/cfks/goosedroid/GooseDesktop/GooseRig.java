package com.cfks.goosedroid.GooseDesktop;

import com.cfks.goosedroid.SamEngine.*;

/**
 * Advanced skeletal rig for the cat.
 * Handles body parts, expressions, animations, and poses.
 */
public class GooseRig {

    public static class Bone {
        public Vector2 position;
        public float rotation;
        public float length;
        public float radius;

        public Bone(float radius, float length) {
            this.radius = radius;
            this.length = length;
            this.position = Vector2.zero;
            this.rotation = 0f;
        }

        public Vector2 getEndPoint(Vector2 direction) {
            return Vector2.add(position, Vector2.multiply(direction, length));
        }
    }

    public enum Expression {
        NEUTRAL,    
        HAPPY,      
        SAD,        
        SLEEPY,     
        SURPRISED,  
        ANGRY,      
        LOVE        
    }

    public enum Pose {
        NORMAL,     
        ALERT,      
        RELAXED,    
        SLEEPING,   
        EATING,     
        EXCITED,    
        SCARED      
    }

    public final Bone underbody;
    public final Bone body;
    public final Bone neck;
    public final Bone head1;
    public final Bone head2;
    public final Bone beak;
    public final Bone leftEye;
    public final Bone rightEye;
    public final Bone leftWing;
    public final Bone rightWing;
    public final Bone tail;
    public final Bone leftEar;
    public final Bone rightEar;

    public Vector2 underbodyCenter;
    public Vector2 bodyCenter;
    public Vector2 neckBase;
    public Vector2 neckHeadPoint;
    public Vector2 head1EndPoint;
    public Vector2 head2EndPoint;
    public Vector2 beakEnd;
    public Vector2 leftEyePos;
    public Vector2 rightEyePos;
    public Vector2 leftWingPos;
    public Vector2 rightWingPos;
    public Vector2 tailPos;
    public Vector2 tailEnd;
    public Vector2 leftEarPos;
    public Vector2 rightEarPos;

    private float wingFlapPhase = 0f;
    private float wingFlapSpeed = 0f;
    private float tailWagPhase = 0f;
    private float earTwitchPhase = 0f;

    private float neckLerpPercent = 0f;
    private float breathPhase = 0f;
    private float blinkTimer = 0f;
    private float blinkDuration = 0f;
    private boolean isBlinking = false;
    private float expressionBlend = 0f;
    private float poseBlend = 0f;

    private Expression currentExpression = Expression.NEUTRAL;
    private Expression targetExpression = Expression.NEUTRAL;
    private Pose currentPose = Pose.NORMAL;
    private Pose targetPose = Pose.NORMAL;

    private float squashAmount = 0f;
    private float targetSquash = 0f;
    private float stretchAmount = 0f;
    private float targetStretch = 0f;
    private float squashRecoverySpeed = 5f;

    private Vector2 tailLag = new Vector2(0, 0);
    private Vector2 wingLag = new Vector2(0, 0);
    private float bellyWobble = 0f;
    private float bellyWobbleVelocity = 0f;
    private Vector2 lastPosition = new Vector2(0, 0);
    private Vector2 velocity = new Vector2(0, 0);

    private float beakOpenAmount = 0f;
    private float targetBeakOpen = 0f;
    private float browCurve = 0f;
    private float targetBrowCurve = 0f;
    private float tearAmount = 0f;

    private float scale = 1.0f;
    private float targetScale = 1.0f;

    private static final float BREATH_SPEED = 2.5f;
    private static final float BREATH_AMPLITUDE = 0.8f;
    private static final float BLINK_INTERVAL_MIN = 2f;
    private static final float BLINK_INTERVAL_MAX = 5f;
    private static final float BLINK_DURATION = 0.15f;
    private static final float EXPRESSION_BLEND_SPEED = 5f;
    private static final float POSE_BLEND_SPEED = 3f;
    private static final float SCALE_BLEND_SPEED = 2f;

    private static final float SQUASH_RECOVERY_SPEED = 8f;
    private static final float MAX_SQUASH = 0.2f;
    private static final float MAX_STRETCH = 0.15f;

    private static final float TAIL_LAG_FACTOR = 0.2f;
    private static final float TAIL_RECOVERY_SPEED = 5f;
    private static final float WING_LAG_FACTOR = 0.1f;
    private static final float WING_RECOVERY_SPEED = 7f;
    private static final float BELLY_WOBBLE_DAMPING = 3f;
    private static final float BELLY_WOBBLE_SPRING = 15f;

    private static final float BEAK_ANIMATION_SPEED = 8f;
    private static final float BROW_ANIMATION_SPEED = 4f;
    private static final float EAR_TWITCH_SPEED = 6f;

    public GooseRig() {
        // Cat skeleton - rounder, shorter neck
        underbody = new Bone(16, 8);
        body = new Bone(24, 10);      // Wider body
        neck = new Bone(10, 12);      // Shorter neck (was 20)
        head1 = new Bone(14, 3);      // Rounder head
        head2 = new Bone(12, 4);      // Bigger head (was 10, 5)
        beak = new Bone(2, 0);        // Tiny nose (was 4, 3)
        leftEye = new Bone(3, 0);     // Bigger eyes
        rightEye = new Bone(3, 0);
        leftWing = new Bone(6, 12);   // Paw-like (was 8, 18)
        rightWing = new Bone(6, 12);
        tail = new Bone(4, 16);       // Longer, thinner tail
        leftEar = new Bone(3, 8);     // New: ears
        rightEar = new Bone(3, 8);

        resetBlinkTimer();
    }

    public void update(Vector2 position, float direction, float deltaTime) {
        velocity = Vector2.multiply(Vector2.subtract(position, lastPosition), 1f / Math.max(deltaTime, 0.001f));
        lastPosition = new Vector2(position.x, position.y);

        updateBreathing(deltaTime);
        updateBlinking(deltaTime);
        updateExpressionBlend(deltaTime);
        updatePoseBlend(deltaTime);
        updateScaleBlend(deltaTime);

        updateSquashStretch(deltaTime);
        updateSecondaryMotion(deltaTime);
        updateExpressionDetails(deltaTime);
        updateEarTwitch(deltaTime);

        float poseNeckMod = getPoseNeckModifier();
        float poseHeightMod = getPoseHeightModifier();

        float squashScaleX = 1f + squashAmount * 0.15f - stretchAmount * 0.1f;
        float squashScaleY = 1f - squashAmount * 0.15f + stretchAmount * 0.1f;

        Vector2 basePos = new Vector2(position.x, position.y);
        Vector2 scaleVec = new Vector2(1.3f * scale * squashScaleX, 0.4f * scale * squashScaleY);

        Vector2 forward = Vector2.GetFromAngleDegrees(direction);
        forward = Vector2.multiply(forward, scaleVec);
        Vector2 right = Vector2.GetFromAngleDegrees(direction + 90f);
        right = Vector2.multiply(right, scaleVec);

        Vector2 up = new Vector2(0f, -1f);

        float breathOffset = (float) Math.sin(breathPhase) * BREATH_AMPLITUDE * scale;

        // Cat: rounder body, more centered
        float bodyElevation = (12f + breathOffset) * scale * poseHeightMod;
        float underbodyElevation = (8f + breathOffset * 0.5f + bellyWobble) * scale * poseHeightMod;

        underbodyCenter = Vector2.add(basePos, Vector2.multiply(up, underbodyElevation));
        bodyCenter = Vector2.add(basePos, Vector2.multiply(up, bodyElevation));

        // Shorter neck for cat
        float neckHeight = SamMath.Lerp(12f, 6f, neckLerpPercent) * scale * poseNeckMod;
        float neckExtend = SamMath.Lerp(2f, 10f, neckLerpPercent) * scale;

        neckBase = Vector2.add(bodyCenter, Vector2.multiply(forward, 12f * scale));
        neckHeadPoint = Vector2.add(
            Vector2.add(neckBase, Vector2.multiply(forward, neckExtend)),
            Vector2.multiply(up, neckHeight)
        );

        head1EndPoint = Vector2.subtract(
            Vector2.add(neckHeadPoint, Vector2.multiply(forward, 2f * scale)),
            Vector2.multiply(up, 1f * scale)
        );
        head2EndPoint = Vector2.add(head1EndPoint, Vector2.multiply(forward, 4f * scale));

        // Tiny nose
        beakEnd = Vector2.add(head2EndPoint, Vector2.multiply(forward, 1f * scale));

        Vector2 eyeOffset = getExpressionEyeOffset();
        float eyeSpacing = 4f * scale;
        float eyeForward = 3f * scale;
        float eyeUp = 2f * scale;

        Vector2 eyeBase = Vector2.add(
            Vector2.add(neckHeadPoint, Vector2.multiply(up, eyeUp)),
            Vector2.multiply(forward, eyeForward)
        );

        leftEyePos = Vector2.add(
            Vector2.subtract(eyeBase, Vector2.multiply(right, eyeSpacing)),
            eyeOffset
        );
        rightEyePos = Vector2.add(
            Vector2.add(eyeBase, Vector2.multiply(right, eyeSpacing)),
            eyeOffset
        );

        // Cat ears at top of head
        float earHeight = 10f * scale;
        float earSpacing = 5f * scale;
        Vector2 earBase = Vector2.add(neckHeadPoint, Vector2.multiply(up, earHeight));
        
        leftEarPos = Vector2.add(
            Vector2.subtract(earBase, Vector2.multiply(right, earSpacing)),
            Vector2.multiply(Vector2.GetFromAngleDegrees(direction - 30f), 3f * scale)
        );
        rightEarPos = Vector2.add(
            Vector2.add(earBase, Vector2.multiply(right, earSpacing)),
            Vector2.multiply(Vector2.GetFromAngleDegrees(direction + 30f), 3f * scale)
        );

        updateWings(deltaTime);
        float wingAngle = getWingAngle();
        Vector2 wingOffset = Vector2.multiply(up, 10f * scale);

        leftWingPos = Vector2.add(
            Vector2.subtract(bodyCenter, Vector2.multiply(right, 9f * scale)),
            wingOffset
        );

        rightWingPos = Vector2.add(
            Vector2.add(bodyCenter, Vector2.multiply(right, 9f * scale)),
            wingOffset
        );

        updateTail(deltaTime);
        Vector2 tailOffset = Vector2.multiply(forward, -14f * scale);
        tailPos = Vector2.add(bodyCenter, tailOffset);

        float tailAngle = getTailWagAngle();
        Vector2 tailDirection = Vector2.add(
            Vector2.multiply(forward, -1f),
            Vector2.multiply(right, tailAngle * 0.4f)
        );

        Vector2 tailLagOffset = Vector2.multiply(tailLag, scale * 0.5f);
        tailEnd = Vector2.add(
            Vector2.add(tailPos, Vector2.multiply(Vector2.Normalize(tailDirection), 14f * scale)),
            tailLagOffset
        );

        underbody.position = underbodyCenter;
        body.position = bodyCenter;
        neck.position = neckBase;
        head1.position = neckHeadPoint;
        head2.position = head1EndPoint;
        beak.position = head2EndPoint;
        leftEye.position = leftEyePos;
        rightEye.position = rightEyePos;
        leftWing.position = leftWingPos;
        rightWing.position = rightWingPos;
        tail.position = tailPos;
        leftEar.position = leftEarPos;
        rightEar.position = rightEarPos;
    }

    private void updateWings(float deltaTime) {
        if (wingFlapSpeed > 0) {
            wingFlapPhase += wingFlapSpeed * deltaTime;
            if (wingFlapPhase > Math.PI * 2) {
                wingFlapPhase -= (float)(Math.PI * 2);
            }
        } else {
            wingFlapPhase = breathPhase * 0.3f;
        }
    }

    private void updateTail(float deltaTime) {
        float wagSpeed = 2f;
        if (currentPose == Pose.EXCITED) wagSpeed = 6f;
        else if (currentExpression == Expression.HAPPY) wagSpeed = 4f;

        tailWagPhase += wagSpeed * deltaTime;
        if (tailWagPhase > Math.PI * 2) {
            tailWagPhase -= (float)(Math.PI * 2);
        }
    }

    private void updateEarTwitch(float deltaTime) {
        earTwitchPhase += EAR_TWITCH_SPEED * deltaTime;
        if (earTwitchPhase > Math.PI * 2) {
            earTwitchPhase -= (float)(Math.PI * 2);
        }
    }

    public float getWingAngle() {
        if (wingFlapSpeed > 0) {
            return (float)Math.sin(wingFlapPhase) * 25f;
        } else {
            return (float)Math.sin(breathPhase) * 3f;
        }
    }

    public float getTailWagAngle() {
        float amplitude = 0.4f;
        if (currentPose == Pose.EXCITED) amplitude = 0.9f;
        else if (currentExpression == Expression.HAPPY) amplitude = 0.6f;
        return (float)Math.sin(tailWagPhase) * amplitude;
    }

    public float getEarTwitchAngle() {
        return (float)Math.sin(earTwitchPhase) * 15f;
    }

    public void setWingFlapSpeed(float speed) {
        this.wingFlapSpeed = Math.max(0, Math.min(20f, speed));
    }

    public float getWingFlapPhase() {
        return wingFlapPhase;
    }

    public void update(Vector2 position, float direction) {
        update(position, direction, Time.deltaTime);
    }

    private void updateBreathing(float deltaTime) {
        breathPhase += BREATH_SPEED * deltaTime;
        if (breathPhase > Math.PI * 2) {
            breathPhase -= (float) (Math.PI * 2);
        }
    }

    private void updateBlinking(float deltaTime) {
        blinkTimer -= deltaTime;

        if (blinkTimer <= 0 && !isBlinking) {
            isBlinking = true;
            blinkDuration = BLINK_DURATION;
        }

        if (isBlinking) {
            blinkDuration -= deltaTime;
            if (blinkDuration <= 0) {
                isBlinking = false;
                resetBlinkTimer();
            }
        }
    }

    private void resetBlinkTimer() {
        blinkTimer = SamMath.RandomRange(BLINK_INTERVAL_MIN, BLINK_INTERVAL_MAX);
    }

    private void updateExpressionBlend(float deltaTime) {
        if (currentExpression != targetExpression) {
            expressionBlend += EXPRESSION_BLEND_SPEED * deltaTime;
            if (expressionBlend >= 1f) {
                expressionBlend = 0f;
                currentExpression = targetExpression;
            }
        }
    }

    private void updatePoseBlend(float deltaTime) {
        if (currentPose != targetPose) {
            poseBlend += POSE_BLEND_SPEED * deltaTime;
            if (poseBlend >= 1f) {
                poseBlend = 0f;
                currentPose = targetPose;
            }
        }
    }

    private void updateScaleBlend(float deltaTime) {
        if (Math.abs(scale - targetScale) > 0.01f) {
            scale = SamMath.Lerp(scale, targetScale, SCALE_BLEND_SPEED * deltaTime);
        } else {
            scale = targetScale;
        }
    }

    private float getPoseNeckModifier() {
        float baseMod = getPoseNeckModifierForPose(currentPose);
        if (currentPose != targetPose) {
            float targetMod = getPoseNeckModifierForPose(targetPose);
            return SamMath.Lerp(baseMod, targetMod, poseBlend);
        }
        return baseMod;
    }

    private float getPoseNeckModifierForPose(Pose pose) {
        switch (pose) {
            case ALERT:     return 1.2f;
            case RELAXED:   return 0.8f;
            case SLEEPING:  return 0.5f;
            case EATING:    return 0.6f;
            case EXCITED:   return 1.0f;
            case SCARED:    return 0.7f;
            default:        return 1.0f;
        }
    }

    private float getPoseHeightModifier() {
        float baseMod = getPoseHeightModifierForPose(currentPose);
        if (currentPose != targetPose) {
            float targetMod = getPoseHeightModifierForPose(targetPose);
            return SamMath.Lerp(baseMod, targetMod, poseBlend);
        }
        return baseMod;
    }

    private float getPoseHeightModifierForPose(Pose pose) {
        switch (pose) {
            case ALERT:     return 1.15f;
            case RELAXED:   return 0.95f;
            case SLEEPING:  return 0.7f;
            case EATING:    return 0.9f;
            case EXCITED:   return 1.08f + (float) Math.sin(breathPhase * 3) * 0.04f;
            case SCARED:    return 0.8f;
            default:        return 1.0f;
        }
    }

    private Vector2 getExpressionEyeOffset() {
        Vector2 baseOffset = getExpressionEyeOffsetForExpression(currentExpression);
        if (currentExpression != targetExpression) {
            Vector2 targetOffset = getExpressionEyeOffsetForExpression(targetExpression);
            return Vector2.Lerp(baseOffset, targetOffset, expressionBlend);
        }
        return baseOffset;
    }

    private Vector2 getExpressionEyeOffsetForExpression(Expression expr) {
        switch (expr) {
            case HAPPY:     return new Vector2(0, -1f * scale);
            case SAD:       return new Vector2(0, 1.5f * scale);
            case SLEEPY:    return new Vector2(0, 0.5f * scale);
            case SURPRISED: return new Vector2(0, -1.5f * scale);
            case ANGRY:     return new Vector2(0, 0.5f * scale);
            case LOVE:      return new Vector2(0, 0);
            default:        return Vector2.zero;
        }
    }

    public float getNeckLerpPercent() {
        return neckLerpPercent;
    }

    public void setNeckLerpPercent(float percent) {
        this.neckLerpPercent = percent;
    }

    public boolean isBlinking() {
        return isBlinking;
    }

    public float getBlinkAmount() {
        if (!isBlinking) return 0f;
        float progress = 1f - (blinkDuration / BLINK_DURATION);
        return (float) Math.sin(progress * Math.PI);
    }

    public Expression getCurrentExpression() {
        return currentExpression;
    }

    public Pose getCurrentPose() {
        return currentPose;
    }

    public float getScale() {
        return scale;
    }

    public float getBreathPhase() {
        return breathPhase;
    }

    public void setExpression(Expression expression) {
        if (expression != targetExpression) {
            targetExpression = expression;
            expressionBlend = 0f;
        }
    }

    public void setPose(Pose pose) {
        if (pose != targetPose) {
            targetPose = pose;
            poseBlend = 0f;
        }
    }

    public void setScale(float newScale) {
        targetScale = Math.max(0.5f, Math.min(2.0f, newScale));
    }

    public void setScaleImmediate(float newScale) {
        scale = Math.max(0.5f, Math.min(2.0f, newScale));
        targetScale = scale;
    }

    public void setExpressionImmediate(Expression expression) {
        currentExpression = expression;
        targetExpression = expression;
        expressionBlend = 0f;
    }

    public void setPoseImmediate(Pose pose) {
        currentPose = pose;
        targetPose = pose;
        poseBlend = 0f;
    }

    public float getEffectiveEyeRadius() {
        float baseRadius = leftEye.radius * scale;
        if (isBlinking) {
            return baseRadius * (1f - getBlinkAmount() * 0.8f);
        }
        if (currentExpression == Expression.SLEEPY) {
            return baseRadius * 0.6f;
        }
        return baseRadius;
    }

    public float getEyeVerticalScale() {
        if (isBlinking) {
            return 1f - getBlinkAmount() * 0.9f;
        }
        switch (currentExpression) {
            case HAPPY:     return 0.4f;
            case SAD:       return 1.2f;
            case SLEEPY:    return 0.2f;
            case SURPRISED: return 1.5f;
            case ANGRY:     return 0.5f;
            default:        return 1.0f;
        }
    }

    public boolean hasHeartEyes() {
        return currentExpression == Expression.LOVE || targetExpression == Expression.LOVE;
    }

    public float getBreathBodyScale() {
        return 1f + (float) Math.sin(breathPhase) * 0.02f;
    }

    private void updateSquashStretch(float deltaTime) {
        squashAmount = SamMath.Lerp(squashAmount, targetSquash, SQUASH_RECOVERY_SPEED * deltaTime);
        stretchAmount = SamMath.Lerp(stretchAmount, targetStretch, SQUASH_RECOVERY_SPEED * deltaTime);

        targetSquash = SamMath.Lerp(targetSquash, 0f, squashRecoverySpeed * deltaTime);
        targetStretch = SamMath.Lerp(targetStretch, 0f, squashRecoverySpeed * deltaTime);

        squashAmount = SamMath.Clamp(squashAmount, 0f, MAX_SQUASH);
        stretchAmount = SamMath.Clamp(stretchAmount, 0f, MAX_STRETCH);
    }

    public void triggerSquash(float amount) {
        targetSquash = Math.min(amount, MAX_SQUASH);
        bellyWobbleVelocity += amount * 10f;
    }

    public void triggerStretch(float amount) {
        targetStretch = Math.min(amount, MAX_STRETCH);
    }

    public float getSquashAmount() {
        return squashAmount;
    }

    public float getStretchAmount() {
        return stretchAmount;
    }

    private void updateSecondaryMotion(float deltaTime) {
        float speed = Vector2.Distance(Vector2.zero, velocity);

        Vector2 targetTailLag = Vector2.multiply(velocity, -TAIL_LAG_FACTOR);
        tailLag = Vector2.Lerp(tailLag, targetTailLag, TAIL_RECOVERY_SPEED * deltaTime);

        Vector2 targetWingLag = Vector2.multiply(velocity, -WING_LAG_FACTOR);
        wingLag = Vector2.Lerp(wingLag, targetWingLag, WING_RECOVERY_SPEED * deltaTime);

        float springForce = -BELLY_WOBBLE_SPRING * bellyWobble;
        float dampingForce = -BELLY_WOBBLE_DAMPING * bellyWobbleVelocity;

        bellyWobbleVelocity += (springForce + dampingForce) * deltaTime;
        bellyWobble += bellyWobbleVelocity * deltaTime;

        if (speed > 50f) {
            bellyWobbleVelocity += (float)Math.sin(Time.time * 15f) * speed * 0.001f;
        }

        bellyWobble = SamMath.Clamp(bellyWobble, -2f, 2f);
    }

    public Vector2 getTailLag() {
        return tailLag;
    }

    public Vector2 getWingLag() {
        return wingLag;
    }

    public float getBellyWobble() {
        return bellyWobble;
    }

    private void updateExpressionDetails(float deltaTime) {
        beakOpenAmount = SamMath.Lerp(beakOpenAmount, targetBeakOpen, BEAK_ANIMATION_SPEED * deltaTime);
        browCurve = SamMath.Lerp(browCurve, targetBrowCurve, BROW_ANIMATION_SPEED * deltaTime);

        switch (currentExpression) {
            case SURPRISED:
                targetBeakOpen = 0.2f;
                targetBrowCurve = 0.4f;
                break;
            case HAPPY:
                targetBeakOpen = 0.05f;
                targetBrowCurve = 0.2f;
                break;
            case SAD:
                targetBeakOpen = 0f;
                targetBrowCurve = -0.6f;
                tearAmount = SamMath.Lerp(tearAmount, 1f, deltaTime);
                break;
            case ANGRY:
                targetBeakOpen = 0.1f;
                targetBrowCurve = -0.5f;
                break;
            case SLEEPY:
                targetBeakOpen = 0.02f;
                targetBrowCurve = -0.1f;
                break;
            case LOVE:
                targetBeakOpen = 0.15f;
                targetBrowCurve = 0.3f;
                break;
            default:
                targetBeakOpen = 0f;
                targetBrowCurve = 0f;
                tearAmount = SamMath.Lerp(tearAmount, 0f, deltaTime * 2f);
                break;
        }

        beakOpenAmount = SamMath.Clamp(beakOpenAmount, 0f, 1f);
        browCurve = SamMath.Clamp(browCurve, -1f, 1f);
        tearAmount = SamMath.Clamp(tearAmount, 0f, 1f);
    }

    public void setBeakOpen(float amount) {
        targetBeakOpen = SamMath.Clamp(amount, 0f, 1f);
    }

    public float getBeakOpenAmount() {
        return beakOpenAmount;
    }

    public float getBrowCurve() {
        return browCurve;
    }

    public float getTearAmount() {
        return tearAmount;
    }

    public void triggerEatingAnimation() {
        float eatPhase = (float)Math.sin(Time.time * 8f);
        targetBeakOpen = 0.2f + eatPhase * 0.15f;
    }

    public void triggerAnticipation(int type) {
        switch (type) {
            case 1:
                triggerSquash(0.1f);
                targetBrowCurve = 0.3f;
                break;
            case 2:
                neckLerpPercent = Math.max(0, neckLerpPercent - 0.2f);
                break;
            default:
                triggerSquash(0.05f);
                break;
        }
    }

    public void reset() {
        currentExpression = Expression.NEUTRAL;
        targetExpression = Expression.NEUTRAL;
        currentPose = Pose.NORMAL;
        targetPose = Pose.NORMAL;
        scale = 1.0f;
        targetScale = 1.0f;
        breathPhase = 0f;
        isBlinking = false;
        resetBlinkTimer();

        squashAmount = 0f;
        targetSquash = 0f;
        stretchAmount = 0f;
        targetStretch = 0f;
        tailLag = new Vector2(0, 0);
        wingLag = new Vector2(0, 0);
        bellyWobble = 0f;
        bellyWobbleVelocity = 0f;
        beakOpenAmount = 0f;
        targetBeakOpen = 0f;
        browCurve = 0f;
        targetBrowCurve = 0f;
        tearAmount = 0f;
    }
}
