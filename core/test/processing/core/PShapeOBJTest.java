package processing.core;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

/*
* Unit tests for PShapeOBJ class
* */
public class PShapeOBJTest {

    /*
    * Test rgbaValue method with white color
    * */
    @Test
    public void testRgbaValueWhiteColor() {
        PVector whiteColor = new PVector(1.0f, 1.0f, 1.0f);
        int rgba = PShapeOBJ.rgbaValue(whiteColor);

        // White: 0xFF
        Assert.assertEquals(0xFFFFFFFF, rgba);
    }

    /*
    * Test addMaterial method
    * */
    @Test
    public void testAddMaterials(){
        ArrayList<PShapeOBJ.OBJMaterial> materialsList = new ArrayList<>();
        HashMap<String, Integer> materialHash = new HashMap<>();

        PShapeOBJ.OBJMaterial material = PShapeOBJ.addMaterial("testMaterial",materialsList, materialHash);

        Assert.assertEquals("testMaterial",material.name);
        Assert.assertEquals(1,materialHash.size());
        Assert.assertEquals(1, materialsList.size());
    }

    /*
    * Test addMaterial method with multiple value
    * */
    @Test
    public void testAddMaterialsMultiple(){
        ArrayList<PShapeOBJ.OBJMaterial> materialsList = new ArrayList<>();
        HashMap<String, Integer> materialHash = new HashMap<>();

        PShapeOBJ.OBJMaterial material1 = PShapeOBJ.addMaterial("testMaterial1",materialsList, materialHash);
        PShapeOBJ.OBJMaterial material2 = PShapeOBJ.addMaterial("testMaterial2",materialsList, materialHash);
        PShapeOBJ.OBJMaterial material3 = PShapeOBJ.addMaterial("testMaterial3",materialsList, materialHash);

        // Checking the size
        Assert.assertEquals(3,materialsList.size());
        Assert.assertEquals(3,materialHash.size());

        // Checking the material name
        Assert.assertEquals("testMaterial1",material1.name);
        Assert.assertEquals("testMaterial2",material2.name);
        Assert.assertEquals("testMaterial3",material3.name);
    }

    /*
    * Test addMaterial with null value
    * */
    @Test
    public void testAddMaterialWithNullValue(){
        ArrayList<PShapeOBJ.OBJMaterial> materialsList = new ArrayList<>();
        HashMap<String, Integer> materialHash = new HashMap<>();

        PShapeOBJ.OBJMaterial material1 = PShapeOBJ.addMaterial(null,materialsList, materialHash);
        PShapeOBJ.OBJMaterial material2 = PShapeOBJ.addMaterial(null,materialsList, materialHash);

        Assert.assertNull(materialsList.get(0).name);
        // Checking null value
        Assert.assertNotNull(materialHash.get(null));
        Assert.assertEquals(1, materialHash.size());
    }

    /*
    * Test addMaterial with duplicate value
    * */
    @Test
    public void testAddMaterialDuplicateNameOverwritesHash(){
        ArrayList<PShapeOBJ.OBJMaterial> materialsList = new ArrayList<>();
        HashMap<String, Integer> materialHash = new HashMap<>();

        PShapeOBJ.addMaterial("material", materialsList, materialHash);
        PShapeOBJ.addMaterial("material", materialsList, materialHash);

        Assert.assertEquals(2, materialsList.size());
        Assert.assertEquals(1, materialHash.size());

        Assert.assertEquals(1, materialHash.get("material").intValue());
    }
}
