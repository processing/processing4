package processing.core;

import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PShapeOBJTest {
    private PApplet parent;
    private ArrayList<PShapeOBJ.OBJFace> faces;
    private ArrayList<PShapeOBJ.OBJMaterial> materials;
    private ArrayList<PVector> coords;
    private ArrayList<PVector> normals;
    private ArrayList<PVector> texcoords;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() {
        parent = new PApplet();
        faces = new ArrayList<>();
        materials = new ArrayList<>();
        coords = new ArrayList<>();
        normals = new ArrayList<>();
        texcoords = new ArrayList<>();
    }

    // Ensure a basic object is parsed correctly - if this fails, something is wrong
    @Test
    public void testBasicOBJParsing() {

        // Define the data to be parsed
        String objData = 
            "v 0.0 0.0 0.0\n" +
            "v 1.0 0.0 0.0\n" +
            "v 0.0 1.0 0.0\n" +
            "f 1 2 3\n";

        // Create a buffered reader, and parse the data
        BufferedReader reader = new BufferedReader(new StringReader(objData));
        PShapeOBJ.parseOBJ(parent, "", reader, faces, materials, coords, normals, texcoords);

        // Ensure the data was parsed correctly - can be expanded on as needed
        assertEquals(3, coords.size());
        assertEquals(1, faces.size());
        assertEquals(3, faces.get(0).vertIdx.size());
    }

    // Ensure a basic material is parsed correctly
    @Test
    public void testMaterialParsing() throws Exception {

        // Define the data to be parsed
        String mtlData = 
            "newmtl Material1\n" +
            "Ka 0.2 0.2 0.2\n" +
            "Kd 0.8 0.8 0.8\n" +
            "Ks 1.0 1.0 1.0\n" +
            "Ns 50.0\n" +
            "d 1.0\n";

        // Create a temporary file with this data
        File mtlFile = tempFolder.newFile("test.mtl");
        try (FileWriter writer = new FileWriter(mtlFile)) {
            writer.write(mtlData);
        }

        // Create a buffered reader with the data, and initialize the materials hash
        BufferedReader reader = new BufferedReader(new StringReader(mtlData));
        Map<String, Integer> materialsHash = new HashMap<>();

        // Parse the data
        PShapeOBJ.parseMTL(parent, mtlFile.getAbsolutePath(), "", reader, materials, materialsHash);

        // Ensure the data was parsed correctly - can be expanded on as needed
        assertEquals(1, materials.size());
        PShapeOBJ.OBJMaterial material = materials.get(0);
        assertEquals("Material1", material.name);
        assertEquals(0.2f, material.ka.x, 0.001f);
        assertEquals(0.8f, material.kd.y, 0.001f);
        assertEquals(1.0f, material.ks.z, 0.001f);
        assertEquals(50.0f, material.ns, 0.001f);
        assertEquals(1.0f, material.d, 0.001f);
    }

    // Ensure verticies and normals are parsed correctly for a basic object
    @Test
    public void testVertexNormalParsing() {

        // Define the data to be parsed
        String objData = 
            "v 0.0 0.0 0.0\n" +
            "v 1.0 0.0 0.0\n" +
            "v 0.0 1.0 0.0\n" +
            "vn 0.0 0.0 1.0\n" +
            "vn 0.0 1.0 0.0\n" +
            "vn 1.0 0.0 0.0\n" +
            "f 1//1 2//2 3//3\n";

        // Create a buffered reader, and parse the data
        BufferedReader reader = new BufferedReader(new StringReader(objData));
        PShapeOBJ.parseOBJ(parent, "", reader, faces, materials, coords, normals, texcoords);

        // Ensure the data was parsed correctly - can be expanded on as needed
        assertEquals(3, normals.size());
        assertEquals(3, faces.get(0).normIdx.size());
    }

    // Ensure parsing properly handles texture coordinates as well
    @Test
    public void testTextureCoordinateParsing() {

        // Define the data to be parsed
        String objData = 
            "v 0.0 0.0 0.0\n" +
            "v 1.0 0.0 0.0\n" +
            "v 0.0 1.0 0.0\n" +
            "vt 0.0 0.0\n" +
            "vt 1.0 0.0\n" +
            "vt 0.0 1.0\n" +
            "f 1/1 2/2 3/3\n";
        // Create a buffered reader with the data, and parse the data
        BufferedReader reader = new BufferedReader(new StringReader(objData));
        PShapeOBJ.parseOBJ(parent, "", reader, faces, materials, coords, normals, texcoords);

        // Ensure the data was parsed correctly - can be expanded on as needed
        assertEquals(3, texcoords.size());
        assertEquals(3, faces.get(0).texIdx.size());
    }

    // Ensure conversion from PVector to 32-bit color int works correctly
    @Test
    public void testRGBAValueConversion() {

        // Define the PVector color
        PVector color = new PVector(1.0f, 0.5f, 0.0f);

        // Convert to 32-bit color int
        int rgba = PShapeOBJ.rgbaValue(color);

        // Ensure the conversion was correct - note alpha is always 0xFF here
        assertEquals(0xFF, (rgba >> 24) & 0xFF); // A
        assertEquals(0xFF, (rgba >> 16) & 0xFF); // R
        assertEquals(0x7F, (rgba >> 8) & 0xFF);  // G
        assertEquals(0x00, rgba & 0xFF);         // B
    }

    // Ensure conversion from PVector to 32-bit color int (with alpha) works correctly
    @Test
    public void testRGBAValueConversionWithAlpha() {

        // Define the PVector color, and alpha
        PVector color = new PVector(1.0f, 0.5f, 0.0f);
        float alpha = 0.5f;

        //Convert to 32-bit color int
        int rgba = PShapeOBJ.rgbaValue(color, alpha);

        // Ensure the conversion was correct
        assertEquals(0x7F, (rgba >> 24) & 0xFF); // A
        assertEquals(0xFF, (rgba >> 16) & 0xFF); // R
        assertEquals(0x7F, (rgba >> 8) & 0xFF);  // G
        assertEquals(0x00, rgba & 0xFF);         // B
    }

    // Ensure a newly-created OBJFace is initialized correctly
    @Test
    public void testOBJFaceCreation() {

        // Create the empty OBJFace
        PShapeOBJ.OBJFace face = new PShapeOBJ.OBJFace();

        // Verify attributes are initialized correctly - can be expanded on as needed
        assertTrue(face.vertIdx.isEmpty());
        assertTrue(face.texIdx.isEmpty());
        assertTrue(face.normIdx.isEmpty());
        assertEquals(-1, face.matIdx);
        assertEquals("", face.name);
    }

    // Ensure a newly-created OBJFace is initialized correctly
    @Test
    public void testOBJMaterialCreation() {

        // Create the empty OBJMaterial
        PShapeOBJ.OBJMaterial material = new PShapeOBJ.OBJMaterial("TestMaterial");

        // Verify attributes are initialized correctly - can be expanded on as needed
        assertEquals("TestMaterial", material.name);
        assertEquals(0.5f, material.ka.x, 0.001f);
        assertEquals(0.5f, material.kd.y, 0.001f);
        assertEquals(0.5f, material.ks.z, 0.001f);
        assertEquals(1.0f, material.d, 0.001f);
        assertEquals(0.0f, material.ns, 0.001f);
        assertNull(material.kdMap);
    }

    // After previous tests, ensure a more complex shape, with verticies, materials, and normals is parsed correctly
    @Test
    public void testComplexFaceParsing() {

        // Define the data to be parsed
        String objData = 
            "v 0.0 0.0 0.0\n" +
            "v 1.0 0.0 0.0\n" +
            "v 0.0 1.0 0.0\n" +
            "v 1.0 1.0 0.0\n" +
            "vt 0.0 0.0\n" +
            "vt 1.0 0.0\n" +
            "vt 0.0 1.0\n" +
            "vt 1.0 1.0\n" +
            "vn 0.0 0.0 1.0\n" +
            "f 1/1/1 2/2/1 3/3/1 4/4/1\n";

        // Create a buffered reader with the data, and parse the data
        BufferedReader reader = new BufferedReader(new StringReader(objData));
        PShapeOBJ.parseOBJ(parent, "", reader, faces, materials, coords, normals, texcoords);

        // Ensure the data was parsed correctly - can be expanded on as needed
        assertEquals(1, faces.size());
        PShapeOBJ.OBJFace face = faces.get(0);
        assertEquals(4, face.vertIdx.size());
        assertEquals(4, face.texIdx.size());
        assertEquals(4, face.normIdx.size());
    }
}