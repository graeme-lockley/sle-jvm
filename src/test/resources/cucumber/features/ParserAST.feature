Feature: The parse tree is translated into an abstract parse tree

  Scenario: Expression "true" as an AST
    Given the sle text "true"
    When I parse the input as a factor
    Then I expect the AST to be "(True)"
