package de.usd.cstchef.operations.dataformat;

import java.util.HashMap;

import org.javatuples.Pair;
import org.junit.Before;
import org.junit.Test;

import burp.CstcObjectFactory;
import burp.api.montoya.core.ByteArray;
import de.usd.cstchef.operations.OperationCategory;
import de.usd.cstchef.utils.UnitTestObjectFactory;
import de.usd.cstchef.operations.Operation.OperationInfos;

import static org.junit.Assert.assertArrayEquals;

@OperationInfos(name = "ToDecimalTest", category = OperationCategory.DATAFORMAT, description = "Test class")
public class ToDecimalTest extends ToDecimal {
    
    HashMap<String, Pair<String, Boolean>> inputs = new HashMap<String, Pair<String, Boolean>>();

    @Test
    public void extractionTest() throws Exception
    {
        for(String inp : inputs.keySet()){
            Pair<String, Boolean> res = inputs.get(inp);
            ByteArray inputArray = factory.createByteArray(inp);
            if(res.getValue1()) {
                // add Exception cases
            }
            else {
                assertArrayEquals(factory.createByteArray(res.getValue0()).getBytes(), perform(inputArray).getBytes());
            }
        }
    }

    @Before
    public void setup(){
        CstcObjectFactory factory = new UnitTestObjectFactory();
        this.factory = factory;
        super.factory = factory;

        // input = Test
        String input1 = "To Decimal";
        String output1 = "84 111 32 68 101 99 105 109 97 108";
        Pair<String, Boolean> pair1 = new Pair<String, Boolean>(output1, false);

        // add more test cases

        inputs.put(input1, pair1);
    }

}
