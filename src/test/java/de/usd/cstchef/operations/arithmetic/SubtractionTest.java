package de.usd.cstchef.operations.arithmetic;

import org.junit.Before;
import org.junit.Test;

import burp.BurpUtils;
import burp.CstcObjectFactory;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.internal.MontoyaObjectFactory;
import de.usd.cstchef.operations.Operation.OperationInfos;
import de.usd.cstchef.operations.Operation;
import de.usd.cstchef.operations.OperationCategory;
import de.usd.cstchef.operations.arithmetic.Addition;
import de.usd.cstchef.operations.arithmetic.Subtraction;
import de.usd.cstchef.utils.UnitTestObjectFactory;

@OperationInfos(name = "Test", category = OperationCategory.ARITHMETIC, description = "Test class")
public class SubtractionTest extends Subtraction
{
    private String number;
    private boolean isFloat;

    protected double getNumber()
    {
        return Double.valueOf(number);
    }

    protected boolean isFloat()
    {
        return isFloat;
    }

    @Test
    public void SimpleSubtraction() throws Exception
    {
        number = "10";
        isFloat = false;

        String testValue = "22";
        ByteArray result = perform(factory.createByteArray(testValue), null);

        assert result.toString().equals("12");
    }

    @Test
    public void SubtractionFloatTest() throws Exception
    {
        number = "2.1";
        isFloat = true;

        String testValue = "2.2";
        ByteArray result = perform(factory.createByteArray(testValue), null);

        System.out.println(result.toString());
        assert result.toString().startsWith("0.1");
    }

    @Test
    public void SubractionRoundTest() throws Exception
    {
        number = "2.2";
        isFloat = false;

        String testValue = "2.8";
        ByteArray result = perform(factory.createByteArray(testValue), null);

        assert result.toString().equals("1");
    }

    @Before
    public void setup(){
        CstcObjectFactory factory = new UnitTestObjectFactory();
        this.factory = factory;
        super.factory = factory;
    }
}