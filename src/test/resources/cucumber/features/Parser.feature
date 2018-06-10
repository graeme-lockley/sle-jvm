Feature: A collection of scenarios that demonstrate the SLE parser

  Scenario: A simple declaration is parsed
    Given the sle text "identity x = x"
    When I parse the supplied input as a module
    Then the parse tree is "(module (declaration identity x = (expression (factor x))))"