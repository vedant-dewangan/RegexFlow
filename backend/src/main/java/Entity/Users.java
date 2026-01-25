package Entity;

import Enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long UId;

    @Column(nullable = false)
    private String Name;

    @Column(unique = true,nullable = false)
    private String Email;

    @Column(nullable = false,name = "password_hash")
    private String PasswordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole Role;

    @OneToMany(mappedBy = "VerifiedBy")
    private List<AuditLog> AuditLogs;

    @OneToMany(mappedBy = "CreatedBy")
    private List<RegexTemplate> RegexTemplates;

}
