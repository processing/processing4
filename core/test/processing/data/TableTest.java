package processing.data;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TableTest {

    class Person {
        public String name;
        public int age;

        public Person() {
            name = "";
            age = -1;
        }
    }


    Person[] people;

    @Test
    public void parseInto() {
        Table table = new Table();
        table.addColumn("name");
        table.addColumn("age");

        TableRow row = table.addRow();
        row.setString("name", "Person1");
        row.setInt("age", 30);

        table.parseInto(this, "people");

        Assert.assertEquals(people[0].name, "Person1");
        Assert.assertEquals(people[0].age, 30);
    }

    @Test
    public void testGetMaxFloat() {
        Table table = new Table();
        table.addColumn("col1", Table.FLOAT);
        table.addColumn("col2", Table.FLOAT);
        table.addColumn("col3", Table.FLOAT);

        //Normal case with positive values
        TableRow row1 = table.addRow();
        row1.setFloat("col1", 5.5f);
        row1.setFloat("col2", 10.2f);
        row1.setFloat("col3", 3.7f);

        TableRow row2 = table.addRow();
        row2.setFloat("col1", 15.8f);
        row2.setFloat("col2", 2.1f);
        row2.setFloat("col3", 8.9f);

        assertEquals(15.8f, table.getMaxFloat(), 0.001f);

        //Table with negative values
        Table table2 = new Table();
        table2.addColumn("col1", Table.FLOAT);
        TableRow row3 = table2.addRow();
        row3.setFloat("col1", -5.5f);
        TableRow row4 = table2.addRow();
        row4.setFloat("col1", -2.3f);

        assertEquals(-2.3f, table2.getMaxFloat(), 0.001f);

        //Table with missing values (NaN)
        Table table3 = new Table();
        table3.addColumn("col1", Table.FLOAT);
        table3.addColumn("col2", Table.FLOAT);

        TableRow row5 = table3.addRow();
        row5.setFloat("col1", Float.NaN);
        row5.setFloat("col2", 7.5f);

        TableRow row6 = table3.addRow();
        row6.setFloat("col1", 12.3f);
        row6.setFloat("col2", Float.NaN);

        assertEquals(12.3f, table3.getMaxFloat(), 0.001f);

        //Table with all missing values
        Table table4 = new Table();
        table4.addColumn("col1", Table.FLOAT);
        TableRow row7 = table4.addRow();
        row7.setFloat("col1", Float.NaN);
        TableRow row8 = table4.addRow();
        row8.setFloat("col1", Float.NaN);

        assertTrue(Float.isNaN(table4.getMaxFloat()));

        //Empty table
        Table table5 = new Table();
        table5.addColumn("col1", Table.FLOAT);

        assertTrue(Float.isNaN(table5.getMaxFloat()));

        //Single value
        Table table6 = new Table();
        table6.addColumn("col1", Table.FLOAT);
        TableRow row9 = table6.addRow();
        row9.setFloat("col1", 42.0f);

        assertEquals(42.0f, table6.getMaxFloat(), 0.001f);
    }
}
