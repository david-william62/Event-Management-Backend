package com.college.eventmanagement.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "event_results")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventResults {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  
}