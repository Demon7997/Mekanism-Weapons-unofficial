package mekanism.weapons.common.module;

public interface IWeaponModule {
    /**
     * Imposta il livello di potenza del modulo.
     * @param level Il livello da impostare (0=OFF, 1=LOW, 2=MEDIUM, ecc.)
     */
    void setLevel(int level);
}
