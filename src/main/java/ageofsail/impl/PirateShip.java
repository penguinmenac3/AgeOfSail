package ageofsail.impl;

import ageofsail.Direction;
import ageofsail.Ship;
import ageofsail.SailAmount;
import ageofsail.World;

/**
 * A pirate ship. Arrrrrrrr....
 *
 * @author Michael Fürst
 * @version 1.0
 * @since 2014-05-12
 */
public class PirateShip implements Ship{
    private static final int SHOOTING_DIRECTIONS = 0;

    /**
     * The turning speed of a ship.
     */
    private static double TURN_SPEED = 1.0;

    /**
     * Acceleration of the ship.
     */
    private static double ACCELERATION = 1.0;

    /**
     * Limit the max speed a ship can have, if it is more, it dies... :)
     */
    private static double MAX_SPEED = 1.0;

    /**
     * The recoil of the cannons.
     */
    private static double RECOIL = 1.0;

    private final int id;
    private final double maxHealth;
    private int health = 0;
    private int loot = 0;
    private double x;
    private double y;
    private double speed;
    private double direction;
    private double[] reloadTime = new double [SHOOTING_DIRECTIONS];

    private double desiredHeading;
    private SailAmount speedLevel;
    private World world;

    public PirateShip(final int id, final World world, final int health, final int latitude, final int longitude) {
        this.id = id;
        this.world = world;
        this.maxHealth = health;
        this.health = health;
        this.x = longitude;
        this.y = latitude;
    }

    @Override
    synchronized public int getId() {
        return id;
    }

    @Override
    synchronized public void update(final double elapsedTime) {

        // Update the direction of the ship.
        if (desiredHeading != direction) {
            double dif = desiredHeading - direction;
            dif = dif > 180.0 ? dif - 360.0 : dif;
            dif = dif > TURN_SPEED * elapsedTime ? TURN_SPEED * elapsedTime : dif;
            direction += dif;
            direction = (direction + 360.0) % 360.0;
        }

        // Update the speed of the ship.
        // FIXME I would say the health speed modifier with health/maxHealth is ok.
        double calcSpeed = applyWindPhysics((health/maxHealth) * speedLevel.getModifier() * world.getWindSpeed(), direction);
        if (calcSpeed != speed) {
            double dif = speed - calcSpeed;
            double mod = world.getWindSpeed();
            dif = dif > ACCELERATION * mod * elapsedTime ? ACCELERATION * mod * elapsedTime : dif;
            dif = dif < -ACCELERATION * mod * elapsedTime ? -ACCELERATION * mod * elapsedTime : dif;
            speed += dif;
        }

        // The ship capsizes if it is too fast.
        if (speed >= MAX_SPEED) {
            health = 0;
        }

        // Reload guns
        for (int i = 0; i < reloadTime.length; i++) {
            reloadTime[i] -= elapsedTime;
            reloadTime[i] = reloadTime[i] > 0 ? reloadTime[i] : 0;
        }

        // Now finaly move that ship.
        x += speed * Math.cos(Math.toRadians(direction+90.0));
        y += speed * Math.sin(Math.toRadians(direction+90.0));
    }

    private double applyWindPhysics(final double speed, final double direction) {
        // Check if the direction is correct, otherwise apply no wind direction.
        if (world.getWindDirection() < 0.0 || world.getWindDirection() >= 360.0) {
            return speed;
        }
        // Calculate in what direction we are to the wind.
        double dif = world.getWindDirection() - direction;
        dif = dif > 180.0 ? dif - 360 : dif;
        dif = Math.abs(dif);

        assert dif >= 0.0 && dif <= 180.0;

        // Now modify our speed.
        double res = speed;
        if (dif < 45.0) {
            res = 0;
        } else if (dif < 65) {
            res *= 0.4;
        } else if (dif < 115) {
            res *= 0.6;
        } else if (dif < 160) {
            res *= 1.0;
        } else {
            res *= 0.8;
        }
        return res;
    }

    @Override
    synchronized public boolean fire(final Direction direction) {
        if (reloadTime[direction.getId()] == 0) {
            reloadTime[direction.getId()] = RECOIL;
            return true;
        }
        return false;
    }

    @Override
    synchronized public void setDesiredHeading(final double angle) {
        desiredHeading = angle;
    }
    
    @Override
    public double getDesiredHeading() {
        return desiredHeading;
    }

    @Override
    synchronized public double getHeading() {
        return direction;
    }

    @Override
    synchronized public void setSailAmount(final SailAmount speed) {
        speedLevel = speed;
    }

    @Override
    synchronized public SailAmount getSailAmount() {
        return speedLevel;
    }

    @Override
    synchronized public double getLatitude() {
        return y;
    }

    @Override
    synchronized public double getLongitude() {
        return x;
    }

    @Override
    synchronized public boolean isDead() {
        return health <= 0;
    }

    @Override
    synchronized public int getHealth() {
        return health;
    }

    @Override
    synchronized public void damage(int damage) {
        health -= damage;
    }

    @Override
    public int getLoot() {
        return loot;
    }

    @Override
    public void addLoot(int collected) {
        loot += collected;
    }
}
