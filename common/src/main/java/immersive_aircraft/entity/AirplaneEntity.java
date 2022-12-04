package immersive_aircraft.entity;

import immersive_aircraft.entity.misc.AircraftProperties;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Implements airplane like physics properties and accelerated towards
 */
public abstract class AirplaneEntity extends EngineAircraft {
    private final AircraftProperties properties = new AircraftProperties()
            .setYawSpeed(5.0f)
            .setPitchSpeed(4.0f)
            .setEngineSpeed(0.0225f)
            .setGlideFactor(0.05f)
            .setDriftDrag(0.01f)
            .setLift(0.15f)
            .setRollFactor(45.0f)
            .setGroundPitch(4.0f)
            .setWindSensitivity(0.01f)
            .setMass(15.0f);

    public AirplaneEntity(EntityType<? extends AircraftEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public AircraftProperties getProperties() {
        return properties;
    }

    @Override
    float getGroundVelocityDecay() {
        return 0.975f;
    }

    @Override
    protected float getGravity() {
        Vec3d direction = getDirection();
        float speed = (float)((float)getVelocity().length() * (1.0f - Math.abs(direction.getY())));
        return Math.max(0.0f, 1.0f - speed * 2.0f) * super.getGravity();
    }

    private void updateEnginePowerTooltip() {
        if (getPrimaryPassenger() instanceof ClientPlayerEntity player) {
            player.sendMessage(Text.translatable("immersive_aircraft.engine_target", (int)(getEngineTarget() * 100.f + 0.5f)), true);
        }
    }

    float getBrakeFactor() {
        return 0.95f;
    }

    @Override
    void updateController() {
        if (!hasPassengers()) {
            return;
        }

        super.updateController();

        // engine control
        if (movementY != 0) {
            setEngineTarget(Math.max(0.0f, Math.min(1.0f, getEngineTarget() + 0.1f * movementY)));
            updateEnginePowerTooltip();
            if (movementY < 0) {
                setVelocity(getVelocity().multiply(getBrakeFactor()));
            }
        }

        // get direction
        Vec3d direction = getDirection();

        // speed
        float thrust = (float)(Math.pow(getEnginePower(), 2.0) * properties.getEngineSpeed());

        // accelerate
        setVelocity(getVelocity().add(direction.multiply(thrust)));
    }
}
