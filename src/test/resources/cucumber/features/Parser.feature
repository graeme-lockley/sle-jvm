Feature: A collection of scenarios that demonstrate the SLE parser

  Scenario: A simple declaration is parsed
    Given the sle text "identity x = x"
    When I parse the supplied input as a module
    Then the parse tree is "(module (declaration identity x = (expression (factor x))))"


  Scenario: Multiplicatives have higher precedence that additives
    Given the sle text "f a b c = a + b * c"
    When I parse the supplied input as a module
    Then the parse tree is "(module (declaration f a b c = (expression (expression (factor a)) + (expression (expression (factor b)) * (expression (factor c))))))"


  Scenario: A syntax error is reported
    Given the sle text "identity 10 = x"
    When I parse the supplied input as a module
    Then I expect a syntax error to be reported