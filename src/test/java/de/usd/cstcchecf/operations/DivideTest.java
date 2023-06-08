package de.usd.cstcchecf.operations;

import org.junit.Test;

import de.usd.cstchef.operations.Operation.OperationInfos;
import de.usd.cstchef.operations.OperationCategory;
import de.usd.cstchef.operations.arithmetic.Divide;

@OperationInfos(name = "Test", category = OperationCategory.ARITHMETIC, description = "Test class")
public class DivideTest extends Divide
{
    private String number;
    private boolean isFloat;
    private boolean isReverse;

    protected double getNumber()
    {
        return Double.valueOf(number);
    }

    protected boolean isFloat()
    {
        return isFloat;
    }

    protected boolean isReverse()
    {
        return isReverse;
    }

    @Test
    public void SimpleDivideTest() throws Exception
    {
        number = "2";
        isFloat = false;
        isReverse = false;

        String testValue = "4";
        byte[] result = perform(testValue.getBytes());

        assert new String(result).equals("2");
    }

    @Test
    public void ReverseDivideTest() throws Exception
    {
        number = "2";
        isFloat = true;
        isReverse = true;

        String testValue = "4";
        byte[] result = perform(testValue.getBytes());

        assert new String(result).equals("0.5");
    }
}