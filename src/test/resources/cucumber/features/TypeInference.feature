Feature: Generation of constraints

  Scenario: A constant int factor will generate no constraints with it's parse tree labeled "Int"
    Given the sle text "10"
    When I parse the input as a factor
    Then no type constraints are inferred
    And the tree's schema is "[] Int"