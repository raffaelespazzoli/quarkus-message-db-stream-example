package org.acme.database.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;

import io.quarkus.cache.CacheResult;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import lombok.Data;

@Entity(name="accounts")
@Data
@NamedQuery(name = "Account.findByFirstNameAndLastNameOrderByUserName", query = "FROM accounts a WHERE a.firstName = :fisrtName AND a.lastName = :lastName ORDER BY a.userName DESC")
public class Account extends PanacheEntity {

    @Column(name = "username")
    private String userName;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "age")
    private Integer age;

    @CacheResult(cacheName = "DESCENDING_QUERY")
    public static List<Account> findByFirstNameAndLastNameOrderByUserName(final String firstName, final String lastName){
      return list("#Account.findByFirstNameAndLastNameOrderByUserName", firstName,lastName);
    }
    @CacheResult(cacheName = "ACCOUNT_STATE")
    public static Account findByUserName(final String userName){
      return find("username", userName).firstResult();
    }  
}