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
class Operation:
    def __init__(self, op, operation_function=None):
        if op[0] == '!':
            non_commutative_flip = True
            op = op[1:]
        else:
            non_commutative_flip = False
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
    
    @staticmethod
    def get_non_commutative_flips(ops):
        new_ops = []
        for op in ops:
            if not op.is_commutative():
                new_ops.append(Operation('!' + op.op, operation_function=op.operation_function))
        return new_ops

    @classmethod
    def default_operations_dict(cls):
        return {
            "+": cls("+"),
            "-": cls("-"),
            "*": cls("*"),  
            "/": cls("/", operation_function=lambda a, b: float(a) / b if b != 0 else None),
           # "!-": cls("!-"),
           # "!/": cls("!/", operation_function=lambda a, b: float(a) / b if b != 0 else None)
        }
    @classmethod
    def commutative_operations_dict(cls):
        return {
            "+": cls("+"),
            "*": cls("*"),  
        }
    @classmethod
    def default_operations(cls):
        return list(cls.default_operations_dict().values())
    @classmethod
    def commutative_operations(cls):
        return list(cls.commutative_operations_dict().values())
    

class Expression:
    def __init__(self,expression,abstract=False,rounding=None,rpn=False):
        
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

        self.abstract = abstract
        self.rounding = rounding
        self.rpn = rpn

        if not self.abstract:
            self.value = self.evaluate()
        else:
            self.num_values = 0
            for token in self.expression:
                if isinstance(token,int):
                    self.num_values = max(self.num_values,token+1)
    def __call__(self,*values):
        if self.abstract:
            if len(values) != self.num_values:
                raise ValueError(f"Expected {self.num_values} values, but got {len(values)}")
            list_expression = list(self.expression)
            for value_to_replace, value_to_insert in enumerate(values):
                for i,value in enumerate(self.expression):
                    if value == value_to_replace:
                        list_expression[i] = value_to_insert
            return Expression(tuple(list_expression),abstract=False,rounding=self.rounding,rpn=self.rpn)
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
            return hash(str(Expression.convert_to_parenthetical(self).expression))
        else:
            return hash(str(self.expression))
    def __eq__(self,other):
        if isinstance(other,Expression):
            return self.expression == other.expression and self.rpn == other.rpn
        else:
            return self.value == other
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
            return Expression(stack[0],abstract=expression.abstract,rounding=expression.rounding,rpn=False)
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
        
            
class ExpressionList:
    def __init__(self,expressions,rounding=5,generic_expressions=False,rpn=False,ops = None):
        self.expressions = expressions
        self.rounding = rounding
        self.generic_expressions = generic_expressions
        self.rpn = rpn
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
    def __call__(self,*values):
        return ExpressionList.create_non_generic_expression_list(self,values)
    def get_values(self):
        values = []
        for expression in self.expressions:
            values.append(expression.value)
        return values
    @staticmethod
    def create_non_generic_expression_list(generic_expression_list,values):
        '''
        creates a new expression list with the given values and a generic expression list.
        '''
        if not generic_expression_list.generic_expressions:
            raise ValueError("Input must be a generic expression list")
        new_expressions = []
        for expression in generic_expression_list:
            new_expressions.append(expression(*values))
        return ExpressionList(new_expressions,rounding=generic_expression_list.rounding,generic_expressions=False,rpn=generic_expression_list.rpn,ops=generic_expression_list.ops)
    

     
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
        def assemble_rpn_expression(values,operations,operation_order,abstract=False):
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

            return Expression(tuple(values[0]),abstract=abstract,rounding=self.rounding,rpn=True)
        
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
        
        operation_orders = list(ExpressionPowerSet.permutations([i for i in range(num_operations)]))
        for operations in reversed(operations_list):
            for operation_order_indexes in operation_orders:
                for given_values,truncator_values in values_and_truncators_permutations:
                    
                    
                    next_expression = assemble_rpn_expression(truncator_values,operations,operation_order_indexes,abstract=False)
                    #print(next_expression.value)
                    if next_expression.value not in seen:
                        if next_expression.value is not None:
                            seen.add(next_expression.value)
                        yield assemble_rpn_expression(given_values,operations,operation_order_indexes,abstract=self.generate_generic_expressions)
                    
            
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
                    truncator_expression = Expression(tuple(expression_truncator), abstract=False, rounding=self.rounding, rpn=False)
                    if truncator_expression.value is None or truncator_expression.value not in seen: 
                        #print(truncator_expression, truncator_expression.value)
                        next_expression = Expression(tuple(expression_values), abstract=self.generate_generic_expressions, rounding=self.rounding, rpn=False)
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
        if ops_left is None:
            ops_left = num_ops
        if op<len(ops)-1:
            for i in range(ops_left+1):
                start = [ops[op] for _ in range(i)]
                yield from [start+x for x in ExpressionPowerSet.generate_operation_combinations(num_ops,ops,op+1,ops_left-i)]
        else:
            yield [ops[op] for _ in range(ops_left)]
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
    def get_operation_locations(num_ops):
        def generate_non_increasing(current, remaining, max_val):
            if remaining == 0:
                yield current
            else:
                for i in range(min(max_val+1,remaining)):
                    yield from generate_non_increasing(current+[i], remaining - 1, i)

        return list(generate_non_increasing([], num_ops, num_ops))
            
        
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
    def __init__(self, num_values=4, operations=None, attempts=15, expressions=None, rpn=False,rounding=5):
        if expressions is None:
            expression_list = ExpressionPowerSet.create_expressions_that_cover(num_values, rpn=rpn, operations=operations, output_new_expressions=False, attempts=attempts)
        else:
            expression_list = ExpressionList(expressions,rounding=rounding,generic_expressions=True,rpn=rpn,ops=operations)
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
            return solutions

    def __str__(self):
        return str(self.expressions)
    def create_valid_question(self,value_range=(1,15),max_attempts=1E6):
        solutions = []
        attempts = 0
        while not solutions and attempts < max_attempts:
            values = [random.randint(value_range[0],value_range[1]) for _ in range(self.num_values)]
            solutions = self.solve(*values)
            attempts += 1
        return values,solutions
    def generate_set_of_valid_questions(self,num_questions=100,value_range=(1,15),max_attempts=1E6):
        for _ in range(num_questions):
            yield self.create_valid_question(value_range,max_attempts)

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
    

        
#smallest_generator = Solver.generate_solver_from_values([67,71,19,41])


import time

# Measure time for RPN expressions
start_time = time.time()
expression_power_set = ExpressionPowerSet(rpn=True, rounding=5, values=5)
rpn_expressions = [str(expression) for expression in expression_power_set.get_expression_list()]
rpn_time = time.time() - start_time

# Measure time for parenthetical expressions
start_time = time.time()
expression_power_set = ExpressionPowerSet(rpn=False, rounding=5, values=5)
parenthetical_expressions = [str(expression) for expression in expression_power_set.get_expression_list()]
parenthetical_time = time.time() - start_time

print(f"Time taken for RPN expressions: {rpn_time:.4f} seconds")
print(f"Time taken for parenthetical expressions: {parenthetical_time:.4f} seconds")


print(len(rpn_expressions),len(parenthetical_expressions))

'''
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


'''
