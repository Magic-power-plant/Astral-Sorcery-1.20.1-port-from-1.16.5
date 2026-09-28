package hellfirepvp.astralsorcery.common.crystal;

public interface CrystalPropertyModifierFunction {

    double modify(double value, double originalValue, int propertyLevel, CalculationContext context);
}
