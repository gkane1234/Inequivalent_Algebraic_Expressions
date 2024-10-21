'''
parentheses = [axbxcxd]

123 --> ((axb)xc)xd
132 --> (axb)x(cxd)
213 --> (ax(bxc))xd
231 --> ax((bxc)xd)
312 --> (axb)x(cxd) -- same as 132
321 --> ax(bx(cxd))


5 ways to parenthesize 4 numbers. Given by permutations of 123 on the operations. One of the permutations is redundant.

to find the set of all ways to do operations on the 4 numbers we take the number of permutations of the numbers, look at each way
to order the operations, and each way to order the parentheses.

'''

import itertools
import random
import time
import tkinter as tk
from tkinter import messagebox

class Operation:
    def __init__(self, op, operation_function=None,commutative=False):

        if op[0] == '!':
            non_commutative_flip = True
            op = op[1:]
        else:
            non_commutative_flip = False
        self.commutative = commutative
        self.op = op
        if operation_function is None:
            if non_commutative_flip:
                self.operation_function = lambda a, b: eval(f"{b}{op}{a}")
            else:
                self.operation_function = lambda a, b: eval(f"{a}{op}{b}")
        else:
            self.operation_function = operation_function
        self.non_commutative_flip = non_commutative_flip
    
    def __call__(self, a, b):
        return self.operation_function(a, b)
    
    def __str__(self):
        if self.non_commutative_flip:
            return '!' + self.op
        else:   
            return self.op

    def __repr__(self):
        return str(self)

    @classmethod
    def default_operations_dict(cls):
        return {
            "+": cls("+",operation_function=lambda a, b: a+b,commutative=True),
            "-": cls("-",operation_function=lambda a, b: a-b,commutative=False),
            "*": cls("*",operation_function=lambda a, b: a*b,commutative=True),  
            "/": cls("/", operation_function=lambda a, b: float(a) / b if b != 0 else None,commutative=False),
           # "!-": cls("!-"),
           # "!/": cls("!/", operation_function=lambda a, b: float(a) / b if b != 0 else None)
        }
    @classmethod
    def commutative_operations_dict(cls):
        return {
            "+": cls("+",commutative=True),
            "*": cls("*",commutative=True),  
        }
    @classmethod
    def default_operations(cls):
        return list(cls.default_operations_dict().values())
    @classmethod
    def commutative_operations(cls):
        return list(cls.commutative_operations_dict().values())
    

class Expression:
    def __init__(self,expression,generic_expression=False,rounding=None,rpn=False):
        
        '''
        expression is a tuple of operations and numbers written in reverse polish notation.

        Abstract expressions are expressions that are not fully evaluated. They are used to represent the operations that are to be performed.
        Variables are repressented by integers and are indexed starting from 0.
        '''
        if isinstance(expression,str):
            self.expression = Expression.create_expression_from_string(expression)
        elif isinstance(expression,(list,tuple)):
            self.expression = tuple(expression)
        else:
            self.expression = None 

        self.generic_expression = generic_expression
        self.rounding = rounding
        self.rpn = rpn

        if not self.generic_expression:
            self.value = self.evaluate()
        else:
            self.num_values = 0
            for token in self.expression:
                if isinstance(token,int):
                    self.num_values = max(self.num_values,token+1)
    def __call__(self,*values,generic_expression=False):
        if self.generic_expression:
            if len(values) != self.num_values:
                raise ValueError(f"Expected {self.num_values} values, but got {len(values)}")
            list_expression = list(self.expression)
            for value_to_replace, value_to_insert in enumerate(values):
                for i,value in enumerate(self.expression):
                    if value == value_to_replace:
                        list_expression[i] = value_to_insert
            return Expression(tuple(list_expression),generic_expression=generic_expression,rounding=self.rounding,rpn=self.rpn)
        else:
            if len(values) != 0:
                raise ValueError(f"Expected 0 values, but got {len(values)}. Expression has no unknown values to substitute.")
            return self.value
    def __iter__(self):
        return iter(self.expression)
    def __str__(self):
        if self.rpn:
            return "".join([str(x) for x in Expression.convert_to_parenthetical(self).expression])
        else:
            return "".join([str(x) for x in self.expression])
    def __repr__(self):
        return str(self)
    def __hash__(self):
        if self.rpn:
            return hash(str(self.expression))
        else:
            return hash(str(self.expression))
    def __eq__(self,other):
        if isinstance(other,Expression):
            return self.expression == other.expression and self.rpn == other.rpn
        else:
            return self.value == other
    def __len__(self):
        return len(self.expression)
    def evaluate(self):
        '''
        evaluates the expression and returns the result. If the expression is not valid, it returns None.
        Will also return None if the expression involves division by zero.
        '''
        if self.rpn:
            return self.evaluate_rpn()
        else:
            return self.evaluate_parenthetical()
                
    def evaluate_rpn(self):
        stack = []
        for token in self.expression:
            if isinstance(token, (int, float)):
                stack.append(token)
            else:
                if len(stack) < 2:
                    raise ValueError(f"Invalid expression: {self.expression}")
                b = float(stack.pop())
                a = float(stack.pop())
                result = token(a, b)
                if result is None:
                    return None
                stack.append(result)
        if self.rounding is not None:
            return round(stack[0],self.rounding)
        else:
            return stack[0] if stack else None
    def evaluate_parenthetical(self):
        def parse_expression(tokens):
            def parse_term():
                if tokens[0] == '(':
                    tokens.pop(0)  # Remove opening parenthesis
                    result = parse_expression(tokens)
                    if tokens[0] != ')':
                        raise ValueError("Mismatched parentheses")
                    tokens.pop(0)  # Remove closing parenthesis
                    return result
                elif isinstance(tokens[0], (int, float)):
                    return tokens.pop(0)
                else:
                    raise ValueError(f"Unexpected token: {tokens[0]}")

            result = parse_term()
            while tokens and isinstance(tokens[0], Operation):
                op = tokens.pop(0)
                right = parse_term()
                if right is None or result is None:
                    return None
                result = op(result, right)
                if result is None:
                    return None
            return result
        tokens = list(self.expression)
        result = parse_expression(tokens)
        if tokens:
            raise ValueError("Invalid expression")
        if self.rounding is not None and result is not None:
            return round(result, self.rounding)
        else:
            return result
    @staticmethod
    def convert_to_parenthetical(expression):
        '''
        converts an expression object in reverse polish notation to a parenthetical notation.
        '''
        if not expression.rpn:
            return expression
        else:
            if len(expression) == 1:
                return Expression(expression.expression,generic_expression=True,rounding=expression.rounding,rpn=False)
            stack = []
            parenthetical_expression = []
            for token in expression:
                if isinstance(token,(int,float)):
                    stack.append(token)
                else:
                    combined_expression = ['(']
                    b = stack.pop()
                    a = stack.pop()
                    op = token

                    if isinstance(a,list):
                        combined_expression.extend(a)
                    else:
                        combined_expression.append(a)
                    combined_expression.append(op)
                    if isinstance(b,list):
                        combined_expression.extend(b)
                    else:
                        combined_expression.append(b)
                    combined_expression.append(")")
                    stack.append(combined_expression)
            return Expression(stack[0],generic_expression=True,rounding=expression.rounding,rpn=False)
    @staticmethod
    def create_expression_from_string(expression):
        OPERATIONS = Operation.default_operations_dict()
        def add_current_token(token):
            if token in OPERATIONS:
                return OPERATIONS[token]
            else:
                if "." in token:
                    return float(token)
                else:
                    return int(token)
        tokens = []
        current_token = ""
        for char in expression:
            if char in "()":
                if current_token:
                    tokens.append(add_current_token(current_token))
                    current_token = ""
                tokens.append(char)
            elif char in OPERATIONS:
                if current_token:
                    tokens.append(add_current_token(current_token))
                    current_token = ""
                tokens.append(OPERATIONS[char])
            else:
                current_token += char
        if current_token:
            tokens.append(add_current_token(current_token))
        return tuple(tokens)
    @staticmethod
    def  create_combined_expression(expression1,expression2,op):
        def combine_expressions(expression1,expression2,op):
            if expression1.rpn:
                return Expression(tuple(list(expression1.expression)+list(expression2.expression)+[op]),rounding=expression1.rounding,rpn=expression1.rpn,generic_expression=expression1.generic_expression)
            else:
                return Expression(tuple(list(expression1.expression)+[op]+list(expression2.expression)),rounding=expression1.rounding,rpn=expression1.rpn,generic_expression=expression1.generic_expression)
        
        if isinstance(op,list):
            expressions = []
            for operation in op:
                expressions.extend(Expression.create_combined_expression(expression1,expression2,operation))
            return expressions
        if not op.commutative:
            expressions = [combine_expressions(expression1,expression2,op),combine_expressions(expression2,expression1,op)]
        else:
            expressions = [combine_expressions(expression1,expression2,op)]
        return expressions
            
       
            
class ExpressionList:
    def __init__(self,expressions=[],num_values=4,rounding=5,generic_expressions=False,rpn=False,ops = None,num_truncators = 3):
        self.expressions = expressions
        self.num_values = num_values
        self.rounding = rounding
        self.generic_expressions = generic_expressions
        self.rpn = rpn
        self.seen = [set() for _ in range(num_truncators)]
        self.truncators = [[random.random() for _ in range(num_values)] for _ in range(num_truncators)]
        self.num_truncators = num_truncators
        if ops is None:
            self.ops = Operation.default_operations()
        else:
            self.ops = ops.copy()
    def __getitem__(self,i):
        return self.expressions[i]
    def __len__(self):
        return len(self.expressions)
    def __iter__(self):
        return iter(self.expressions)
    def __hash__(self):
        return hash(tuple(self.expressions))
    def __eq__(self,other):
        return self.expressions == other.expressions
    def __call__(self,*values,generic_expressions=False):
        return ExpressionList.create_changed_expression_list(self,values,generic_expressions=generic_expressions,num_truncators=self.num_truncators)
    def __str__(self):
        return str(self.expressions)
    def __repr__(self):
        return str(self)
    def add(self,expression):
        to_add = False
        for i in range(len(self.truncators)):
            value = expression(*self.truncators[i]).value
            if value is not None and value not in self.seen[i]: 
                self.seen[i].add(value)
                to_add = True
        if to_add:
            self.expressions.append(expression)
        return to_add
    def get_values(self):
        values = []
        if self.generic_expressions:
            raise ValueError("Expression list is generic")
        for expression in self.expressions:
            values.append(expression.value)
        return values
    
    @staticmethod
    def create_changed_expression_list(generic_expression_list,values,generic_expressions=False,num_truncators=3):
        '''
        creates a new expression list with the given values and a generic expression list.
        '''
        if not generic_expression_list.generic_expressions:
            raise ValueError("Input must be a generic expression list")
        new_expressions = []
        for expression in generic_expression_list:
            new_expressions.append(expression(*values,generic_expression=generic_expressions))
        return ExpressionList(new_expressions,rounding=generic_expression_list.rounding,generic_expressions=generic_expressions,rpn=generic_expression_list.rpn,ops=generic_expression_list.ops)
    
class ExpressionDynamicProgramming():
    def __init__(self,rounding=5,num_values = 4,rpn=True,ops = None,num_truncators=3):
        if ops is None:
            ops = Operation.default_operations()
        self.num_values = num_values
        self.generate_generic_expressions = True
        self.rounding = rounding
        self.rpn = rpn
        self.ops = ops
        self.num_truncators = num_truncators
    def get_expression_list(self):

        expression_lists = [ExpressionList([],generic_expressions=True,num_values=1,rounding=self.rounding,rpn=self.rpn,ops=self.ops,num_truncators=self.num_truncators)]
        expression_lists[0].add(Expression((0,),rounding=self.rounding,rpn=self.rpn,generic_expression=True))
        for current_num_values in range(2,self.num_values+1):
           
            current_expression_list = ExpressionList([],generic_expressions=True,num_values=current_num_values,rounding=self.rounding,rpn=self.rpn,ops=self.ops,num_truncators=self.num_truncators)
            start = current_num_values-1
            end = current_num_values//2
            if current_num_values%2==0:
                end -=1
            
            for i in range(start,end,-1): #1 indexed 
                
                for combination in itertools.combinations(range(current_num_values),i):
                    remainder = tuple(set(range(current_num_values))-set(combination))
                    
                    new_expressions = ExpressionDynamicProgramming.power_set_of_expressions(expression_lists[i-1](*combination,generic_expressions=True),expression_lists[current_num_values-i-1](*remainder,generic_expressions=True),self.ops)
                    
                    for expression in new_expressions:
                        current_expression_list.add(expression)
            expression_lists.append(current_expression_list)
        return expression_lists[-1]


    @staticmethod
    def power_set_of_expressions(expression_list_1,expression_list_2,ops):
        expressions = []
        #print(expression_list_1,expression_list_2)
        for i in range(len(expression_list_1)):
            for j in range(len(expression_list_2)):
                expression1,expression2 = expression_list_1[i],expression_list_2[j]
                #print(expression1,expression2)
                expressions.extend(Expression.create_combined_expression(expression1,expression2,ops))
        return ExpressionList(expressions,rounding=expression_list_1.rounding,generic_expressions=True,rpn=expression_list_1.rpn,ops=expression_list_1.ops,num_truncators=expression_list_1.num_truncators)
                    


        
     
class ExpressionPowerSet():
    def __init__(self,rounding=5,values = 4,rpn=True,ops = Operation.default_operations()):
        if isinstance(values,int):
            self.values = list(range(values))
            self.generate_generic_expressions = True
        else:
            self.values = values
            self.generate_generic_expressions = False
        self.rounding = rounding
        self.rpn = rpn
        self.ops = ops
    def get_expression_list(self):
        if self.generate_generic_expressions:
            self.values = list(range(len(self.values)))
        if self.rpn:
            self.expression_list = list(self.generate_rpn_expressions())
        else:
            self.expression_list = list(self.generate_parenthetical_expressions())

        return ExpressionList(self.expression_list,rounding=self.rounding,generic_expressions=self.generate_generic_expressions,rpn=self.rpn,ops=self.ops)

    def generate_rpn_expressions(self,truncate_using=None):
        def assemble_rpn_expression(values,operations,operation_order,generic_expression=False):
            if values == (0,2,1):
                print("here")
                print(values,operations,operation_order)
            used = [False]*len(operations)
            values = list(values)
            for operation_index in operation_order:
                
                offset = used[:operation_index].count(True)
                used[operation_index]=True

                operation = operations[operation_index]
                
                operation_index-=offset
                left_partial = values[operation_index] if isinstance(values[operation_index],list) else [values[operation_index]]
                right_partial = values[operation_index+1] if isinstance(values[operation_index+1],list) else [values[operation_index+1]]
                new_partial =left_partial+right_partial+[operation]
                values.pop(operation_index)
                values[operation_index]=new_partial

            return Expression(tuple(values[0]),generic_expression=generic_expression,rounding=self.rounding,rpn=True)
        
        num_values = len(self.values)

        if truncate_using is None:
            if self.generate_generic_expressions:
                truncate_using = [random.random() for _ in range(num_values)]
            else:
                truncate_using = self.values

        print(truncate_using)
        num_operations = num_values - 1

        values_permutations = list(ExpressionPowerSet.distinct_permutations(self.values))

        if self.generate_generic_expressions:
            truncator_permutations = list(ExpressionPowerSet.distinct_permutations(truncate_using))
        else:
            truncator_permutations = values_permutations

        
        
        values_and_truncators_permutations = list(zip(values_permutations, truncator_permutations))

        operations_list = []
        seen = set()

        for arr in self.generate_operation_combinations(num_operations, self.ops):
            operations_list.extend(ExpressionPowerSet.distinct_permutations(arr))
        i=0
        operation_orders = list(ExpressionPowerSet.permutations([i for i in range(num_operations)]))
        for operations in reversed(operations_list):
            for operation_order_indexes in operation_orders:
                for given_values,truncator_values in values_and_truncators_permutations:
                    i+=1
                    
                    next_expression = assemble_rpn_expression(truncator_values,operations,operation_order_indexes,generic_expression=False)
                    #print(next_expression.value)
                    if next_expression.value not in seen:
                        if next_expression.value is not None:
                            seen.add(next_expression.value)
                        if self.generate_generic_expressions:
                            yield assemble_rpn_expression(given_values,operations,operation_order_indexes,generic_expression=True)
                        else:
                            yield next_expression
        print(i)
        yield None
                    
            
    def generate_parenthetical_expressions(self, truncate_using=None):
        num_values = len(self.values)

        if truncate_using is None:
            if self.generate_generic_expressions:
                truncate_using = [random.random() for _ in range(num_values)]
            else:
                truncate_using = self.values

        print(truncate_using)
        num_operations = num_values - 1

        values_permutations = list(ExpressionPowerSet.distinct_permutations(self.values))
        if self.generate_generic_expressions:
            truncator_permutations = list(ExpressionPowerSet.distinct_permutations(truncate_using))
        else:
            truncator_permutations = values_permutations
        
        values_and_truncators_permutations = list(zip(values_permutations, truncator_permutations))

        operations_list = []
        seen = set()

        for arr in self.generate_operation_combinations(num_operations, self.ops):
            operations_list.extend(ExpressionPowerSet.distinct_permutations(arr))
        
        parentheses_list = list(self.generate_parentheses(num_values))
        for ops in reversed(operations_list):
            for expression in parentheses_list:
                for values, truncator in values_and_truncators_permutations:
                    expression_values = list(expression).copy()
                    expression_truncator = list(expression).copy()
                    for i in range(num_values):
                        expression_values[expression_values.index('n')] = values[i]
                        expression_truncator[expression_truncator.index('n')] = truncator[i]
                    for op in ops:
                        expression_values[expression_values.index('x')] = op
                        expression_truncator[expression_truncator.index('x')] = op
                    truncator_expression = Expression(tuple(expression_truncator), generic_expression=False, rounding=self.rounding, rpn=False)
                    if truncator_expression.value is None or truncator_expression.value not in seen: 
                        #print(truncator_expression, truncator_expression.value)
                        next_expression = Expression(tuple(expression_values), generic_expression=self.generate_generic_expressions, rounding=self.rounding, rpn=False)
                        seen.add(truncator_expression.value)
                        yield next_expression
    
    @staticmethod
    def generate_operation_combinations(num_ops,ops,op=0,ops_left=None):

        '''
        num_ops is the number of operations to be performed.
        ops is the list of all possible operations.
        op and ops_left are used in recursion and should not be set by the user.


        generates all possible combinations of operations that can be performed.
        '''
        yield from itertools.combinations_with_replacement(ops,num_ops)
        '''
        if ops_left is None:
            ops_left = num_ops
        if op<len(ops)-1:
            for i in range(ops_left+1):
                start = [ops[op] for _ in range(i)]
                yield from [start+x for x in ExpressionPowerSet.generate_operation_combinations(num_ops,ops,op+1,ops_left-i)]
        else:
            yield [ops[op] for _ in range(ops_left)]
        '''
    @staticmethod
    def generate_parentheses(num_values):
        def surround_op(s,op):
            index=0
            for i in range(op+1):
                index = s.find('x',index+1)
            
            start = index-1
            end = index + 2
            
            if index > 0 and s[index-1] == ')':
                paren_count = 1
                start = index - 2
                while start >= 0 and paren_count > 0:
                    if s[start] == ')':
                        paren_count += 1
                    elif s[start] == '(':
                        paren_count -= 1
                    start -= 1
                start += 1
            
            if index < len(s) - 1 and s[index+1] == '(':
                paren_count = 1
                end = index + 2
                while end < len(s) and paren_count > 0:
                    if s[end] == '(':
                        paren_count += 1
                    elif s[end] == ')':
                        paren_count -= 1
                    end += 1
            
            return f"{s[:start]}({s[start:end]}){s[end:]}"
            
        operations = num_values - 1
        for perm in ExpressionPowerSet.distinct_permutations([i for i in range(operations)]):
            base = "x".join(['n']*num_values)
            for op in perm:
                base = surround_op(base,op)
            yield base
    @staticmethod
    def permutations(arr):
        '''
        generates all permutations of the given array.
        '''
        return itertools.permutations(arr)
        if len(arr) <= 1:
            return [tuple(arr)]  # Return a list containing a tuple
        result = []
        for i in range(len(arr)):
            current = arr[i]
            remaining = arr[:i] + arr[i+1:]
            for perm in ExpressionPowerSet.permutations(remaining):
                result.append(tuple([current] + list(perm)))
        return result

    @staticmethod
    def distinct_permutations(arr):
        '''
        generates all distinct permutations of the given array.
        '''
        seen = set()
        result = []
        for perm in ExpressionPowerSet.permutations(arr):
            if perm not in seen:
                seen.add(perm)
                result.append(perm)
        return result

            
        
    @staticmethod
    def create_expressions_that_cover(num_values,rounding=5,rpn=True,operations=None,attempts=15,distinct_expressions=None, output_new_expressions=False,read_from_file=None,save_to_file=None):
        if read_from_file is not None:
            with open(read_from_file, 'r') as file:
                distinct_expressions = [tuple(line.strip().split(': ')) for line in file]
                distinct_expressions = [(int(i), tuple(expression)) for i, expression in distinct_expressions]
        values = [
                    2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67]
        
        values = [random.random() for _ in range(100)]
        if distinct_expressions is None:
            distinct_values = set()
        else:
            distinct_values = {i for i,expression in distinct_expressions}
        if operations:
            expression_power_set = ExpressionPowerSet(rpn=rpn,rounding=rounding,values=list(range(num_values)),generate_generic_expressions=True,ops=operations)
        else:
            expression_power_set = ExpressionPowerSet(rpn=rpn,rounding=rounding,values=list(range(num_values)),generate_generic_expressions=True)
        #print(len(generic_expressions))
        generic_expressions = expression_power_set.get_expression_list()
        discounted = {}
        chosen_values_list = []
        for attempt in range(attempts):
            
            chosen_values = random.sample(values, num_values)
            print(attempt,chosen_values)
            chosen_values_list.append(chosen_values)
            seen = {}
            evaluated_expressions = generic_expressions(*chosen_values)
            
            for i, expression in enumerate(evaluated_expressions):
                value = expression.value
                if value is not None:
                    #print(value)
                    if value not in seen:
                        if i in discounted or attempt == 0:
                            if output_new_expressions and attempt != 0:
                                old_i = discounted.pop(i)
                                print(f"\n {generic_expressions[i]} vs {generic_expressions[old_i]} \n {evaluated_expressions[i]} vs {evaluated_expressions[old_i]} \n {evaluated_expressions[i].value} vs {evaluated_expressions[old_i].value} \n {generic_expressions[i](*chosen_values_list[-2])} vs {generic_expressions[old_i](*chosen_values_list[-2])}")
                            
                            distinct_values.add(generic_expressions[i])
                            
                        seen[value] = i
                    else:
                        if attempt == 0:
                            discounted[i] = seen[value]

        return ExpressionList(distinct_values,rounding=rounding,generic_expressions=True,rpn=rpn,ops=operations)
    @staticmethod
    def find_smallest_generator(smallest_solver=None,num_values=4,rounding=5,operations=None,save_to_file=None,rpn=False):
        def generate_combinations(n, min_val, max_val):
            return itertools.product(range(min_val, max_val + 1), repeat=n)

        
        if smallest_solver is None:
            if operations:
                expression_power_set = ExpressionPowerSet(rpn=rpn,rounding=rounding,values=list(range(num_values)),generate_generic_expressions=True,ops=operations)
            else:
                expression_power_set = ExpressionPowerSet(rpn=rpn,rounding=rounding,values=list(range(num_values)),generate_generic_expressions=True)
            generic_expressions = expression_power_set.get_expression_list()
        else:
            generic_expressions = smallest_solver
        distinct_values = set()
        values_list = list(generate_combinations(num_values, 1, 10))
        for values in values_list:

            distinct_values = set()
            print(values)
            evaluated_expressions = generic_expressions(*values)
            distinct_values = set(evaluated_expressions.get_values())
            if len(distinct_values) == 1170:
                return values

        return "No solution found"
    @staticmethod
    def find_smallest_generator_from_random_values(smallest_solver=None,num_values=4,rounding=5,operations=None,save_to_file=None,rpn=False):
        values = [random.random() for _ in range(100)]
        if smallest_solver is None:
            if operations:
                expression_power_set = ExpressionPowerSet(rpn=rpn,rounding=rounding,values=list(range(num_values)),generate_generic_expressions=True,ops=operations)
            else:
                expression_power_set = ExpressionPowerSet(rpn=rpn,rounding=rounding,values=list(range(num_values)),generate_generic_expressions=True)
            generic_expressions = expression_power_set.get_expression_list()
        else:
            generic_expressions = smallest_solver
        chosen_values_list = []
        attempt = 0
        distinct_values = set()
        while len(distinct_values) < 1170:
            print(len(distinct_values))
            attempt += 1
            distinct_values = set()
            chosen_values = random.sample(values, num_values)
            print(attempt,chosen_values)
            chosen_values_list.append(chosen_values)
            seen = set()
            evaluated_expressions = generic_expressions(*chosen_values)
            distinct_values = set(evaluated_expressions.get_values())

        if save_to_file is not None:
            with open(save_to_file, 'w') as file:
                for i in distinct_values:
                    file.write(f"{i}: {generic_expressions[i]}\n")

        return distinct_values
 

class Solver(ExpressionList):
    def __init__(self, num_values=4, operations=None, expression_list=None, rpn=True,rounding=5,num_truncators = 3):
        if expression_list is None:
            expression_list = ExpressionDynamicProgramming(rounding=rounding,rpn=rpn,num_values=num_values,num_truncators=num_truncators).get_expression_list()
        else:
            expression_list = ExpressionList(expression_list,rounding=rounding,generic_expressions=True,rpn=rpn,ops=operations)
        self.__dict__ = expression_list.__dict__ #sets this expression list as the super class instance.
        self.num_values = num_values
    def solve(self,*nums,goal=24):
        if hasattr(nums[0], '__iter__'):
            solutions_list=[]
            for vals in nums:
                solutions_list.append(self.solve(*vals,goal=goal))
            return solutions_list
        else:
            if len(nums) != self.num_values:
                raise ValueError(f"This solver is for {self.num_values} values, but got {len(nums)}")
            solutions = set()
            for expression in self.expressions:
                if expression(*nums) == goal:
                    solutions.add(expression(*nums))
            return Question(values=nums,solutions=solutions)

    def __str__(self):
        return str(self.expressions)
    def create_valid_question(self,value_range=(1,15),solution_range=(1,20),max_attempts=1E6):
        values = [random.randint(value_range[0],value_range[1]) for _ in range(self.num_values)]
        solutions = self.solve(*values)
        attempts = 1
        while not (solution_range[0] <= len(solutions.get_solutions()) <= solution_range[1]) and attempts < max_attempts:
            print(solutions)
            print('hi')
            values = [random.randint(value_range[0],value_range[1]) for _ in range(self.num_values)]
            solutions = self.solve(*values)
            attempts += 1
        return values,solutions
    def generate_set_of_valid_questions(self,num_questions=100,value_range=(1,15),solution_range=(1,20),max_attempts=1E6):
        for _ in range(num_questions):
            yield self.create_valid_question(value_range=value_range,solution_range=solution_range,max_attempts=max_attempts)

    @staticmethod
    def verify_solver(solver,redundant_solver,value_range=(1,15),num_questions=100):
        for _ in range(num_questions):
            print(_)
            values = [random.randint(value_range[0],value_range[1]) for _ in range(solver.num_values)]
            seen = set()
            for expression in solver.expressions:
                if expression(*values).value not in seen:
                    seen.add(expression(*values).value)
            for expression in redundant_solver.expressions:
                if expression(*values).value not in seen:
                    print(f"Value {expression(*values).value} from expression {expression(*values)} not found in solver")
                    return False
        return True

    @staticmethod
    def generate_solver_from_values(values,operations=None,rounding=5,rpn=False):
        '''
        creates a solver from a list of generator values
        '''
        if operations:
            generic_expressions = ExpressionPowerSet(rpn=rpn,rounding=rounding,values=list(range(len(values))),generate_generic_expressions=True,ops=operations)
        else:
            generic_expressions = ExpressionPowerSet(rpn=rpn,rounding=rounding,values=list(range(len(values))),generate_generic_expressions=True)
        generic_expressions = generic_expressions.get_expression_list()
        evaluated_expressions = generic_expressions(*values)
        distinct_expressions = []
        seen_values = set()
        for i,expression in enumerate(evaluated_expressions):
            if expression.value is not None and expression.value not in seen_values:
                seen_values.add(expression.value)
                distinct_expressions.append(generic_expressions[i])
        return Solver(num_values=len(values),expressions=distinct_expressions,rpn=rpn)
    @staticmethod
    def create_solver_from_expression_list(expression_list,rpn=False):
        return Solver(num_values=expression_list.num_values,expression_list=expression_list,rpn=rpn)

class QuestionWindow:
    def __init__(self, master, questions, current_index, is_timed_game=False, font=("Arial", 36), background_color="black", text_color="white", goal=24):
        self.master = master
        self.questions = questions
        self.current_index = current_index
        self.is_timed_game = is_timed_game
        self.goal = goal
        self.font = font
        self.background_color = background_color
        self.text_color = text_color

        self.window = tk.Toplevel(master)
        self.window.title(f"Question {self.current_index + 1}")
        self.window.configure(bg=self.background_color)
        self.window.geometry("400x500")

        self.timer_label = tk.Label(self.window, text="", bg=self.background_color, fg=self.text_color)
        self.timer_label.pack(pady=10)

        self.number_frame = tk.Frame(self.window, bg=self.background_color)
        self.number_frame.pack(expand=True)

        self.original_numbers = self.questions[self.current_index][0]
        self.numbers = self.original_numbers.copy()
        self.number_labels = []

        positions = [(1, 0), (0, 1), (2, 1), (1, 2)]
        for i, pos in enumerate(positions):
            label = tk.Label(self.number_frame, text=str(self.numbers[i]), font=self.font, bg=self.background_color, fg=self.text_color)
            label.grid(row=pos[0], column=pos[1], padx=20, pady=20)
            label.bind("<Button-1>", lambda e, idx=i: self.select_number(idx))
            self.number_labels.append(label)

        self.operator_label = tk.Label(self.number_frame, text="", font=self.font, bg=self.background_color, fg=self.text_color)
        self.operator_label.grid(row=1, column=1, padx=20, pady=20)

        self.operator_frame = tk.Frame(self.window, bg=self.background_color)
        self.operator_frame.pack(pady=10)

        operators = ['+', '-', '*', '/']
        for op in operators:
            btn = tk.Button(self.operator_frame, text=op, font=("Arial", 20), command=lambda x=op: self.select_operator(x), bg="gray", fg="black")
            btn.pack(side=tk.LEFT, padx=5)

        self.button_frame = tk.Frame(self.window, bg=self.background_color)
        self.button_frame.pack(pady=20)

        self.reset_button = tk.Button(self.button_frame, text="Reset", command=self.reset_numbers, bg=self.background_color, fg=self.text_color)
        self.reset_button.pack(side=tk.LEFT, padx=10)

        if not self.is_timed_game:
            self.prev_button = tk.Button(self.button_frame, text="Previous", command=self.prev_question, bg=self.background_color, fg=self.text_color)
            self.prev_button.pack(side=tk.LEFT, padx=10)

            self.next_button = tk.Button(self.button_frame, text="Next", command=self.next_question, bg=self.background_color, fg=self.text_color)
            self.next_button.pack(side=tk.LEFT, padx=10)

            self.solution_button = tk.Button(self.button_frame, text="Show Solution", command=self.show_solution, bg=self.background_color, fg=self.text_color)
            self.solution_button.pack(side=tk.LEFT, padx=10)

        self.selected_indices = []
        self.selected_operator = None
        self.operations_history = []

        self.window.bind('<Key>', self.key_press)

        if self.is_timed_game:
            self.start_time = time.time()
            self.update_timer()

    def select_number(self, index):
        if self.numbers[index] is not None:
            if index in self.selected_indices:
                self.selected_indices.remove(index)
                self.number_labels[index].config(font=self.font, fg=self.text_color)
            elif len(self.selected_indices) < 2:
                self.selected_indices.append(index)
                if len(self.selected_indices) == 1:
                    self.number_labels[index].config(font=(self.font[0], self.font[1], "bold"), fg="yellow")
                else:
                    self.number_labels[index].config(font=(self.font[0], self.font[1], "bold"), fg="green")
            self.perform_operation()

    def select_operator(self, operator):
        if self.selected_operator == operator:
            self.selected_operator = None
            self.operator_label.config(text="")
        else:
            self.selected_operator = operator
            self.operator_label.config(text=self.selected_operator)
        self.perform_operation()

    def show_solution(self):
        _, answer = self.questions[self.current_index]
        messagebox.showinfo("Solution", f"The solution is: {answer}")

    def next_question(self):
        if self.current_index <= len(self.questions):
            self.current_index += 1
            self.update_question()

    def prev_question(self):
        if self.current_index > 0:
            self.current_index -= 1
            self.update_question()

    def update_question(self):
        self.window.title(f"Question {self.current_index}")
        self.original_numbers = self.questions[self.current_index-1][0]
        self.reset_numbers()

    def key_press(self, event):
        key = event.char.lower()
        if key in 'wasd':
            self.select_number('awsd'.index(key))
        elif key in '6789':
            operators = {
                '6': '+',
                '7': '-',
                '8': '*',
                '9': '/'
            }
            self.select_operator(operators[key])
        elif key == 'r':
            self.reset_numbers()
        elif key == 'z':
            self.undo_operation()

    def draw_number(self, index, value):
        self.numbers[index] = value
        if value is None:
            self.number_labels[index].config(text="", font=self.font, fg=self.text_color)
        elif int(value)==round(value,3):
            self.number_labels[index].config(text=str(int(value)), font=self.font, fg=self.text_color)
        else:
            self.number_labels[index].config(text=f"{value:.3f}", font=self.font, fg=self.text_color)

    def perform_operation(self):
        if len(self.selected_indices) == 2 and self.selected_operator:
            a, b = [self.numbers[i] for i in self.selected_indices]
            if self.selected_operator == '+':
                result = a + b
            elif self.selected_operator == '-':
                result = a - b
            elif self.selected_operator == '*':
                result = a * b
            elif self.selected_operator == '/':
                result = a / b if b != 0 else None

            if result is not None:
                # Store the current state before making changes
                self.operations_history.append((self.numbers.copy(), self.selected_indices.copy()))

                # Update the first selected number with the result
                first_index = self.selected_indices[0]
                self.numbers[first_index] = result
                self.draw_number(first_index, result)

                # Remove the second selected number
                second_index = self.selected_indices[1]
                self.numbers[second_index] = None
                self.draw_number(second_index, None)

                # Clear the operator
                self.operator_label.config(text="")

                # Reset selection
                self.selected_indices = []
                self.selected_operator = None

                # Reset all number label fonts
                for label in self.number_labels:
                    if label['text']:
                        label.config(font=self.font, fg=self.text_color)

                if self.numbers.count(None) == len(self.numbers)-1:
                    if self.numbers[first_index] == self.goal:
                        print("You win!")
                        if self.is_timed_game:
                            self.next_question()
            else:
                self.undo_operation()

    def reset_numbers(self):
        self.numbers = self.original_numbers.copy()
        for i, label in enumerate(self.number_labels):
            self.draw_number(i, self.numbers[i])
        self.selected_indices = []
        self.selected_operator = None
        self.operator_label.config(text="")
        self.operations_history = []

    def undo_operation(self):
        if self.operations_history:
            previous_state, previous_selected = self.operations_history.pop()
            self.numbers = previous_state
            for i, label in enumerate(self.number_labels):
                if self.numbers[i] is not None:
                    self.draw_number(i, self.numbers[i])
                else:
                    self.draw_number(i, None)
            self.selected_indices = []
            self.selected_operator = None
            self.operator_label.config(text="", fg=self.text_color)

    def update_timer(self):
        if self.is_timed_game:
            elapsed_time = int(time.time() - self.start_time)
            questions_left = len(self.questions) - self.current_index +1
            self.timer_label.config(text="Time: {}s | Questions left: {}".format(elapsed_time, questions_left))
            self.window.after(1000, self.update_timer)

class QuestionUI:
    def __init__(self,questions):
        master = tk.Tk()
        master.title("Math Questions")
        master.configure(bg="black")
        self.master = master
        self.questions = questions
        self.current_page = 0
        self.questions_per_page = 2
        self.game_mode = None
        self.current_question_index = 0
        self.start_time = None
        self.timer_label = None

        self.frame = tk.Frame(master, bg="black")
        self.frame.pack(padx=10, pady=10, fill=tk.BOTH, expand=True)

        self.mode_selection_frame = tk.Frame(self.frame, bg="black")
        self.mode_selection_frame.pack(pady=20)

        tk.Button(self.mode_selection_frame, text="Timed Game", command=lambda: self.set_game_mode("timed"), bg="gray", fg="black").pack(side=tk.LEFT, padx=10)
        tk.Button(self.mode_selection_frame, text="Question Set", command=lambda: self.set_game_mode("set"), bg="gray", fg="black").pack(side=tk.LEFT, padx=10)

        self.game_frame = tk.Frame(self.frame, bg="black")
        self.question_buttons = []
        for i in range(self.questions_per_page):
            btn = tk.Button(self.game_frame, text="", width=30, command=lambda idx=i: self.show_question_window(idx), bg="gray", fg="black")
            btn.grid(row=i//2, column=i%2, padx=5, pady=5)
            self.question_buttons.append(btn)

        self.nav_frame = tk.Frame(self.frame, bg="black")

        self.prev_button = tk.Button(self.nav_frame, text="Previous", command=self.prev_page, bg="gray", fg="black")
        self.prev_button.pack(side=tk.LEFT, padx=5)

        self.next_button = tk.Button(self.nav_frame, text="Next", command=self.next_page, bg="gray", fg="black")
        self.next_button.pack(side=tk.RIGHT, padx=5)

        self.settings_button = tk.Button(self.nav_frame, text="Settings", command=self.show_settings, bg="gray", fg="black")
        self.settings_button.pack(side=tk.BOTTOM, pady=5)
        master.mainloop()

    def set_game_mode(self, mode):
        self.game_mode = mode
        self.mode_selection_frame.pack_forget()
        if mode == "timed":
            self.start_timed_game()
        else:
            self.show_question_set()

    def start_timed_game(self):
        self.current_question_index = 1
        self.start_time = time.time()
        self.show_next_timed_question()

    def show_next_timed_question(self):
        print(self.current_question_index,len(self.questions))
        if self.current_question_index <= len(self.questions):
            self.clear_frame()
            QuestionWindow(self.frame, self.questions, self.current_question_index, is_timed_game=True)
            self.current_question_index += 1
        else:
            self.end_timed_game()

    def clear_frame(self):
        for widget in self.frame.winfo_children():
            widget.destroy()

    def end_timed_game(self):
        end_time = time.time()
        total_time = int(end_time - self.start_time)
        messagebox.showinfo("Game Over", "You completed all questions in {} seconds!".format(total_time))
        self.show_question_set()

    def show_question_set(self):
        self.clear_frame()
        self.game_frame.pack(fill=tk.BOTH, expand=True)
        self.nav_frame.pack(pady=10)
        self.update_questions()

    def update_questions(self):
        start = self.current_page * self.questions_per_page
        end = start + self.questions_per_page
        page_questions = self.questions[start:end]
        for i, btn in enumerate(self.question_buttons):
            if i < len(page_questions):
                question, _ = page_questions[i]
                btn.config(text=f"Question {start+i+1}: {question}", state=tk.NORMAL)
            else:
                btn.config(text="", state=tk.DISABLED)

    def show_question_window(self, idx):
        question_idx = self.current_page * self.questions_per_page + idx
        if question_idx < len(self.questions):
            QuestionWindow(self.frame, self.questions, question_idx, is_timed_game=False)

    def next_page(self):
        if (self.current_page + 1) * self.questions_per_page < len(self.questions):
            self.current_page += 1
            self.update_questions()

    def prev_page(self):
        if self.current_page > 0:
            self.current_page -= 1
            self.update_questions()

    def show_settings(self):
        settings_window = tk.Toplevel(self.master)
        settings_window.title("Settings")
        settings_window.configure(bg="black")

        tk.Label(settings_window, text="Minimum Value:", bg="black", fg="white").grid(row=0, column=0, padx=5, pady=5)
        min_value_entry = tk.Entry(settings_window)
        min_value_entry.insert(0, str(settings.min_value))
        min_value_entry.grid(row=0, column=1, padx=5, pady=5)

        tk.Label(settings_window, text="Maximum Value:", bg="black", fg="white").grid(row=1, column=0, padx=5, pady=5)
        max_value_entry = tk.Entry(settings_window)
        max_value_entry.insert(0, str(settings.max_value))
        max_value_entry.grid(row=1, column=1, padx=5, pady=5)

        tk.Label(settings_window, text="Minimum Solutions:", bg="black", fg="white").grid(row=2, column=0, padx=5, pady=5)
        min_solutions_entry = tk.Entry(settings_window)
        min_solutions_entry.insert(0, str(settings.min_solutions))
        min_solutions_entry.grid(row=2, column=1, padx=5, pady=5)

        tk.Label(settings_window, text="Maximum Solutions:", bg="black", fg="white").grid(row=3, column=0, padx=5, pady=5)
        max_solutions_entry = tk.Entry(settings_window)
        max_solutions_entry.insert(0, str(settings.max_solutions))
        max_solutions_entry.grid(row=3, column=1, padx=5, pady=5)

        tk.Label(settings_window, text="Number of Questions:", bg="black", fg="white").grid(row=4, column=0, padx=5, pady=5)
        num_questions_entry = tk.Entry(settings_window)
        num_questions_entry.insert(0, str(settings.num_questions))
        num_questions_entry.grid(row=4, column=1, padx=5, pady=5)

        def save_settings():
            settings.min_value = int(min_value_entry.get())
            settings.max_value = int(max_value_entry.get())
            settings.min_solutions = int(min_solutions_entry.get())
            settings.max_solutions = int(max_solutions_entry.get())
            settings.num_questions = int(num_questions_entry.get())
            settings_window.destroy()

        def save_and_generate():
            save_settings()
            self.questions = list(solver.generate_set_of_valid_questions(
                num_questions=settings.num_questions,
                value_range=(settings.min_value, settings.max_value),
                solution_range=(settings.min_solutions, settings.max_solutions)
            ))
            self.current_page = 0
            self.update_questions()

        tk.Button(settings_window, text="Save", command=save_settings, bg="gray", fg="black").grid(row=5, column=0, padx=5, pady=5)
        tk.Button(settings_window, text="Save and Generate New Questions", command=save_and_generate, bg="gray", fg="black").grid(row=5, column=1, padx=5, pady=5)

#smallest_generator = Solver.generate_solver_from_values([67,71,19,41])
class Settings:
    def __init__(self):
        self.min_value = 1
        self.max_value = 15
        self.min_solutions = 1
        self.max_solutions = 1000
        self.num_questions = 3
        self.num_values = 5
        self.num_truncators = 4
        self.rounding=5 

class Question:
    def __init__(self,values = None, solutions = None, goal = None) -> None:
        self.values = values
        self.solutions = list(solutions)
        
        if solutions is not None:
            self.num_solutions = len(solutions)
            if goal is None and self.num_solutions>0:
                self.goal = self.solutions[0].value
        else:
            self.num_solutions = 0
        
        if goal is not None:
            self.goal=goal
        else:
            self.goal = None
    
    def set_values(self,values):
        self.values = values
    def get_values(self):
        return self.values
    def set_solutions(self,solutions):
        self.solutions = solutions
    def get_solutions(self):
        return ", ".join(str(self.solutions))
    def __str__(self) -> str:
        return "("+" ".join(f"{val:.3f}" for val in self.values)+")  "+str(len(self.get_solutions()))

settings = Settings()
start_time = time.time()
solver = Solver(num_values = settings.num_values,rounding = settings.rounding, num_truncators=settings.num_truncators,rpn=True)

questions = list(solver.generate_set_of_valid_questions(
        num_questions=settings.num_questions,
        value_range=(settings.min_value, settings.max_value),
        solution_range=(settings.min_solutions, settings.max_solutions)
    ))

end_time = time.time()

print(f"Number of expressions: {len(solver.expressions)}")
print(f"Time taken: {end_time - start_time:.4f} seconds")

print(questions)

# Create the main window


# Create the UI with the generated questions

# Start the Tkinter event loop


'''

# Measure time for RPN expressions
start_time = time.time()
expression_power_set = ExpressionPowerSet(rpn=True, rounding=5, values=4)
rpn_expressions = [str(expression) for expression in expression_power_set.get_expression_list()]
rpn_time = time.time() - start_time

print(f"Time taken for RPN expressions: {rpn_time:.4f} seconds")

# Measure time for parenthetical expressions
start_time = time.time()
expression_power_set = ExpressionPowerSet(rpn=False, rounding=5, values=4)
parenthetical_expressions = [str(expression) for expression in expression_power_set.get_expression_list()]
parenthetical_time = time.time() - start_time


print(f"Time taken for parenthetical expressions: {parenthetical_time:.4f} seconds")


print(len(rpn_expressions),len(parenthetical_expressions))

[67, 71, 19, 41]
[41, 5, 53, 11]
[5, 31, 47, 37]
todo: double check rpn creates all possible expressions.
look into making the checker faster
consider using a database to store expressions to minize memory usage.
1
6
68
1170


for n = 3
dynamic programming would use 36*3=108 values

we use: 6*16*2=192

for n = 4
dynamic programming would use 68*6*4+6*6*6*3= 2280


we use 24*4^3*6 =9216

in general we can find the dynamic programming using sum f(k)*f(n-k)*6*(n Choose k)  where f(k) is the number of distinct expressions with k numbers
and the brute force is n!*4^n*(n-1)!

'''