package project.model;

import java.util.Objects;

/***
 * Class representing a ward with simple fields: id,
 * Building:id, sector, type.
 * The corresponding database table is:
 *    CREATE TABLE Ward (
 *       id int NOT NULL,
 *       building_id int NOT NULL,
 *       sector varchar(50) NOT NULL,
 *       type varchar(50) NOT NULL,
 *       PRIMARY KEY (building_id,sector),
 *       FOREIGN KEY (building_id) REFERENCES Building(id)
 *       UNIQUE KEY ward_id (id)
 *    )
 */
public class Ward {
    private final Integer id;
    private final Integer building_id;
    private final String sector;
    private final String type;

    public Ward(Integer id, Integer building_id, String sector, String type) {
        this.id = id;
        this.building_id = building_id;
        this.sector = sector;
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public Integer getBuilding_id() {
        return building_id;
    }

    public String getSector() {
        return sector;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ward)) return false;
        Ward ward = (Ward) o;
        return Objects.equals(getId(), ward.getId()) && Objects.equals(getBuilding_id(), ward.getBuilding_id()) && Objects.equals(getSector(), ward.getSector()) && Objects.equals(getType(), ward.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getBuilding_id(), getSector(), getType());
    }

    @Override
    public String toString() {
        return "Ward{" +
                "id=" + id +
                ", building_id=" + building_id +
                ", sector='" + sector + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
