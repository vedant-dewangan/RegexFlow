package Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "bank")
@Getter
@Setter
public class Bank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long BId;

    @Column(unique = true, nullable = false)
    private String Name;

    @Column(unique = true, nullable = false)
    private String Address;

    @OneToMany(mappedBy = "Bank")
    private List<RegexTemplate>  RegexTemplates;

}
