package de.usd.cstcchecf.operations;

import org.junit.Test;

import de.usd.cstchef.Delimiter;
import de.usd.cstchef.operations.Operation.OperationInfos;
import de.usd.cstchef.operations.OperationCategory;
import de.usd.cstchef.operations.arithmetic.MultiplyList;

@OperationInfos(name = "Test", category = OperationCategory.ARITHMETIC, description = "Test class")
public class MultiplyListTest extends MultiplyList
{
    private String delimiter;
    private boolean isFloat;

    protected Delimiter getDelimiter() throws IllegalArgumentException
    {
        Delimiter delim = Delimiter.getByName(delimiter);

        if( delim == null )
            throw new IllegalArgumentException("Invalid delimiter.");

        return delim;
    }

    protected boolean isFloat()
    {
        return isFloat;
    }

    @Test
    public void CommaMultiplyTest() throws Exception
    {
        delimiter = "Comma";
        isFloat = false;

        String testValue = "1,2,3,4,5,6";
        byte[] result = perform(testValue.getBytes());

        assert new String(result).equals("720");
    }

    @Test
    public void CommaMultiplyFloatTest() throws Exception
    {
        delimiter = "Comma";
        isFloat = true;

        String testValue = "3,0.5,0.5";
        byte[] result = perform(testValue.getBytes());

        assert new String(result).equals("0.75");
    }

    @Test
    public void SpaceMultiplyTest() throws Exception
    {
        delimiter = "Space";
        isFloat = false;

        String testValue = "1 2 3 4 5 6";
        byte[] result = perform(testValue.getBytes());

        System.out.println(new String(result));
        assert new String(result).equals("720");
    }

    @Test
    public void SpaceMultiplyFloatTest() throws Exception
    {
        delimiter = "Space";
        isFloat = true;

        String testValue = "3 0.5 0.5";
        byte[] result = perform(testValue.getBytes());

        assert new String(result).equals("0.75");
    }
}