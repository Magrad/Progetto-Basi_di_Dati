package project.model;

/***
 * Class representing a drug with simple fields: id, role
 * and specialization
 * The corresponding database table is:
 *    CREATE TABLE Treatment (
 *       id int AUTO_INCREMENT,
 *       name varchar(50) NOT NULL,
 *       type varchar(50) NOT NULL,
 *       description varchar(1000) NOT NULL,
 *       posology varchar(1000) NOT NULL,
 *       allergens varchar(100) NOT NULL,
 *       PRIMARY KEY (id)
 *       UNIQUE (name)
 *    )
 */
public class Treatment {
    private final Integer id;
    private final String name;
    private final String type;
    private final String description;
    private final String posology;
    private final String allergens;

    public Treatment(Integer id, String name, String type, String description, String posology, String allergens) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.posology = posology;
        this.allergens = allergens;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getPosology() {
        return posology;
    }

    public String getAllergens() {
        return allergens;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Treatment)) return false;
        Treatment treatment = (Treatment) o;
        return getId().equals(treatment.getId()) && getName().equals(treatment.getName()) && getType().equals(treatment.getType()) && getDescription().equals(treatment.getDescription()) && getPosology().equals(treatment.getPosology()) && getAllergens().equals(treatment.getAllergens());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(getId(), getName(), getType(), getDescription(), getPosology(), getAllergens());
    }

    @Override
    public String toString() {
        return "Treatment{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", posology='" + posology + '\'' +
                ", allergens='" + allergens + '\'' +
                '}';
    }
}
