package com.sololifeos.explore.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 地点 Entity (DATABASE_DESIGN §6.9, Owner: Explore Module)。
 * <p>
 * 地点为公共参考数据，不做软删除（无 deleted_time 字段）。
 * 索引对齐 §8：idx_city（city）/ idx_lat_lng（latitude, longitude）。
 * <p>
 * 经纬度使用 {@code DECIMAL(10,7)} 精度，Java 侧用 {@link BigDecimal} 映射。
 */
@Entity
@Table(name = "location")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String address;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private LocationType type = LocationType.OTHER;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedTime;

    protected Location() {
        // JPA 规范要求无参构造
    }

    /**
     * 业务构造：创建新地点。
     *
     * @param name      地点名称（非空）
     * @param address   详细地址（可空）
     * @param city      所属城市（非空）
     * @param latitude  纬度（非空）
     * @param longitude 经度（非空）
     * @param type      地点类型（可空，null → OTHER）
     * @return 合法状态的 Location
     * @throws IllegalArgumentException 参数非法时抛出
     */
    public static Location create(String name, String address, String city,
                                  BigDecimal latitude, BigDecimal longitude, LocationType type) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("地点名称不可为空");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("所属城市不可为空");
        }
        if (latitude == null) {
            throw new IllegalArgumentException("纬度不可为空");
        }
        if (longitude == null) {
            throw new IllegalArgumentException("经度不可为空");
        }
        Location location = new Location();
        location.name = name;
        location.address = address;
        location.city = city;
        location.latitude = latitude;
        location.longitude = longitude;
        location.type = type != null ? type : LocationType.OTHER;
        return location;
    }

    /**
     * 更新地点信息。
     */
    public void update(String name, String address, String city, LocationType type) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("地点名称不可为空");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("所属城市不可为空");
        }
        this.name = name;
        this.address = address;
        this.city = city;
        this.type = type != null ? type : LocationType.OTHER;
    }

    // --- getters ---

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public LocationType getType() {
        return type;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    void setId(Long id) {
        this.id = id;
    }

    void setName(String name) {
        this.name = name;
    }

    void setAddress(String address) {
        this.address = address;
    }

    void setCity(String city) {
        this.city = city;
    }

    void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    void setType(LocationType type) {
        this.type = type;
    }
}
