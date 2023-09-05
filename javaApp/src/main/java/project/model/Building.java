package project.model;

import java.util.Objects;

/***
 * Class representing a building with simple fields: id,
 * name, address, city, province, region, phone.
 * The corresponding database table is:
 *    CREATE TABLE Building (
 *       id int AUTO_INCREMENT PRIMARY KEY,
 *       name varchar(50) NOT NULL,
 *       address varchar(50) NOT NULL,
 *       city varchar(50) NOT NULL,
 *       cap int NOT NULL,
 *       province varchar(50) NOT NULL,
 *       region varchar(50) NOT NULL,
 *       phone varchar(50) NOT NULL,
 *       UNIQUE KEY building_name (name)
 *    )
 */
public class Building {
    private final Integer id;
    private final String name;
    private final String address;
    private final String city;
    private final Integer cap;
    private final String province;
    private final String region;
    private final String phone;

    public Building(Integer id, String name, String address, String city, Integer cap, String province, String region, String phone) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.city = city;
        this.cap = cap;
        this.province = province;
        this.region = region;
        this.phone = phone;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() { return address; }

    public String getCity() { return city; }

    public  Integer getCap() { return cap; }

    public String getProvince() { return province; }

    public String getRegion() { return region; }

    public String getPhone() { return phone; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Building)) return false;
        Building building = (Building) o;
        return Objects.equals(getId(), building.getId()) && Objects.equals(getName(), building.getName()) && Objects.equals(getAddress(), building.getAddress()) && Objects.equals(getCity(), building.getCity()) && Objects.equals(getCap(), building.getCap()) && Objects.equals(getProvince(), building.getProvince()) && Objects.equals(getRegion(), building.getRegion()) && Objects.equals(getPhone(), building.getPhone());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getAddress(), getCity(), getCap(), getProvince(), getRegion(), getPhone());
    }

    @Override
    public String toString() {
        return "Building{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", cap=" + cap +
                ", province='" + province + '\'' +
                ", region='" + region + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
