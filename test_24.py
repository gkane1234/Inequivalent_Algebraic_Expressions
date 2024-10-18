import unittest
from operation_counting import *

class TestExpression(unittest.TestCase):
    def test_convert_to_parenthetical(self):
        self.assertEqual(Expression.convert_to_parenthetical(Expression((5,3,Operation("+"),2,Operation("*")), rpn=True)), Expression("((5+3)*2)"))
        self.assertEqual(Expression.convert_to_parenthetical(Expression((10,2,Operation("/"),3,Operation('-')), rpn=True)), Expression("((10/2)-3)"))
        self.assertEqual(Expression.convert_to_parenthetical(Expression((7,4,2,Operation("*"),Operation("+")), rpn=True)), Expression("(7+(4*2))"))
        self.assertEqual(Expression.convert_to_parenthetical(Expression((8,3,2,Operation("/"),Operation('-')), rpn=True)), Expression("(8-(3/2))"))
        self.assertEqual(Expression.convert_to_parenthetical(Expression((6,2,3,Operation("*"),Operation("+"),4,Operation("/")), rpn=True)), Expression("((6+(2*3))/4)"))
        self.assertEqual(Expression.convert_to_parenthetical(Expression((9,5,2,Operation('-'),Operation("*"),3,Operation("+")), rpn=True)), Expression("((9*(5-2))+3)"))
        self.assertEqual(Expression.convert_to_parenthetical(Expression((12,3,Operation("/"),4,2,Operation("*"),Operation('-')), rpn=True)), Expression("((12/3)-(4*2))"))
        self.assertEqual(Expression.convert_to_parenthetical(Expression((15,3,5,Operation("+"),Operation("/"),2,Operation("*")), rpn=True)), Expression("((15/(3+5))*2)"))
        self.assertEqual(Expression.convert_to_parenthetical(Expression((20,4,2,Operation('-'),Operation("/"),3,Operation("+")), rpn=True)), Expression("((20/(4-2))+3)"))
        self.assertEqual(Expression.convert_to_parenthetical(Expression((1,2,3,4,5,Operation("+"),Operation('-'),Operation("*"),Operation("/")), rpn=True)), Expression("(((1+2)-3)*(4/5))"))

TestExpression().test_convert_to_parenthetical()