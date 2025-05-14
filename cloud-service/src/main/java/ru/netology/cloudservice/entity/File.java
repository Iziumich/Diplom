package ru.netology.cloudservice.entity;
import lombok.Data;
import javax.persistence.*;

@Entity
@Data
@Table(name = "files")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String filename;
    @Column(nullable = false)
    private String filepath;
    private Long size;
    @ManyToOne
    private User user;
}