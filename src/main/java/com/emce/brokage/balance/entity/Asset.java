package com.emce.brokage.balance.entity;

import com.emce.brokage.auth.entity.Customer;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Enumerated(EnumType.STRING)
    private AssetType assetName;
    private BigDecimal size;
    private BigDecimal usableSize;
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Asset asset)) return false;
        return Objects.equals(id, asset.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
