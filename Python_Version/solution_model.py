import sqlite3
import pandas as pd
from itertools import combinations_with_replacement;
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor
from tensorflow import keras
from tensorflow.keras import layers

def create_reference_dictionary(range_of_values,num_values) :
    all_tuples = list(combinations_with_replacement(range(range_of_values[0],range_of_values[1]+1),num_values))
    #print(all_tuples)

    return {i+1:tuple for i,tuple in enumerate(all_tuples)}
def load_database(input_file_path,create_new_database=True):
    print("Reading CSV file...")
    with open(input_file_path, 'r') as file:
        metadata_line = file.readline().strip()
        metadata = eval(metadata_line)
    range_of_values = (int(metadata['min_value']), int(metadata['max_value']))
    num_values = metadata['num_values']
    reference_dict = create_reference_dictionary(range_of_values, num_values)
    # Construct database names based on the input file name
    base_name = input_file_path.split('/')[-1].rsplit('.', 1)[0]
    number_sets_db_name = f"counting_operations/outputs/{base_name}.db"
    
    # Connect to the number sets database
    conn_number_sets = sqlite3.connect(number_sets_db_name)
    cursor_number_sets = conn_number_sets.cursor()
    if create_new_database:
        

        # Drop the tables if they exist
        cursor_number_sets.execute("DROP TABLE IF EXISTS number_sets;")
        cursor_number_sets.execute("DROP TABLE IF EXISTS reference_dict;")

        
        create_number_sets_table_query = """
        CREATE TABLE IF NOT EXISTS number_sets (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            outcome INTEGER NOT NULL,
            value_set_id INTEGER,
            FOREIGN KEY (value_set_id) REFERENCES reference_dict(id)
        );
        """
        cursor_number_sets.execute(create_number_sets_table_query)
        
        create_reference_dict_table_query = f"""
        CREATE TABLE IF NOT EXISTS reference_dict (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            {', '.join([f'value{i+1} INTEGER NOT NULL' for i in range(num_values)])}
        );
        """
        cursor_number_sets.execute(create_reference_dict_table_query)
        conn_number_sets.commit()  # Add this line to commit 

        # Read and process the CSV file directly


        # Insert reference dictionary into the database
        for key, value_tuple in reference_dict.items():
            try:
                cursor_number_sets.execute(
                    f"INSERT INTO reference_dict (id, {', '.join([f'value{i+1}' for i in range(num_values)])}) VALUES ({', '.join(['?' for _ in range(num_values + 1)])})",
                    (key, *value_tuple)
                )
            except sqlite3.IntegrityError:
                print(f"Skipping duplicate entry for id: {key}, values: {value_tuple}")
        conn_number_sets.commit()

        with open(input_file_path, 'r') as file:
            # Skip first line (metadata)
            next(file)
            
            # Read and insert data
            for line in file:
                try:
                    # Assuming format is "id,outcome"
                    id_str, outcome_str = line.strip().split(',')
                    if outcome_str:  # Check if outcome is not empty
                        outcome = int(float(outcome_str))
                        value_set_id = int(id_str)
                        cursor_number_sets.execute(
                            "INSERT INTO number_sets (outcome, value_set_id) VALUES (?, ?)",
                            (outcome, value_set_id)
                        )
                except ValueError as e:
                    print(f"Skipping invalid line: {line.strip()} - Error: {e}")
                    continue

        conn_number_sets.commit()
        
        # Print some verification
        cursor_number_sets.execute("SELECT COUNT(*) FROM number_sets")
        count = cursor_number_sets.fetchone()[0]
        print(f"Inserted {count} rows into number_sets table")

        cursor_number_sets.execute("SELECT COUNT(*) FROM reference_dict")
        count = cursor_number_sets.fetchone()[0]
        print(f"Inserted {count} rows into reference_dict table")



        cursor_number_sets.execute("SELECT * FROM number_sets LIMIT 5")
        rows = cursor_number_sets.fetchall()
        for row in rows:
            print(row)

        cursor_number_sets.execute("SELECT * FROM reference_dict LIMIT 5")
        rows = cursor_number_sets.fetchall()
        for row in rows:
            print(row)


    return cursor_number_sets

def get_outcome_and_integers(cursor, value_set_id):
    cursor.execute("SELECT outcome FROM number_sets WHERE value_set_id = ?", (value_set_id,))
    outcome = cursor.fetchone()
    
    if outcome:
        outcome_value = outcome[0]
        cursor.execute("SELECT * FROM reference_dict WHERE id = ?", (value_set_id,))
        integers = cursor.fetchall()
        return outcome_value, integers
    else:
        return None, None



# Function to train a model using the data from the database
def train_model_random_forest(cursor,parameters=None):
    if parameters==None:
        parameters = {'binary_outcome':False,
                      'test_size':0.2,
                      'random_state':42,
                      'n_estimators':1000,
                      'verbose':2,
                      }
    print("Training model...")
    # Fetch data from the database
    cursor.execute("SELECT outcome FROM number_sets")
    if parameters['binary_outcome']:
        outcomes = [(1,) if outcome[0] > 0 else (0,) for outcome in cursor.fetchall()]
    else:
        outcomes = cursor.fetchall()
    
    cursor.execute("SELECT * FROM reference_dict")
    integers = cursor.fetchall()

    # Prepare the data
    X = np.array([list(row[1:]) for row in integers])  # Assuming the first column is an ID
    y = np.array([outcome[0] for outcome in outcomes])

    # Split the data into training and testing sets
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=parameters['test_size'], random_state=parameters['random_state'])

    # Create and train the model

    model = RandomForestRegressor(n_estimators=parameters['n_estimators'], random_state=parameters['random_state'],verbose=parameters['verbose'])
    model.fit(X_train, y_train)

    # Evaluate the model
    score = model.score(X_test, y_test)
    print(f"Model R^2 score: {score}")
    return model


def train_model_neural_network(cursor,parameters=None):

    if parameters==None:
        parameters = {'epochs':10,
              'batch_size':4,
              'validation_split':0.2,
              'hidden_layers':[64]*16,
              'dropout':[0]*15,
              'binary_outcome':False,
              'test_size':0.2,
              'random_state':42,
              'early_stopping':True,
              }
    print("Training model...")
    # Fetch data from the database
    cursor.execute("SELECT outcome FROM number_sets")
    if parameters['binary_outcome']:
        outcomes = [(1,) if outcome[0] > 0 else (0,) for outcome in cursor.fetchall()]
    else:
        outcomes = cursor.fetchall()
    
    cursor.execute("SELECT * FROM reference_dict")
    integers = cursor.fetchall()

    # Prepare the data
    X = np.array([list(row[1:]) for row in integers])  # Assuming the first column is an ID
    y = np.array([outcome[0] for outcome in outcomes])

    # Split the data into training and testing sets
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=parameters['test_size'], random_state=parameters['random_state'])

    # Create and train the model
    '''
    model = RandomForestRegressor(n_estimators=1000, random_state=42)
    model.fit(X_train, y_train)

    # Evaluate the model
    score = model.score(X_test, y_test)
    print(f"Model R^2 score: {score}")
    return model
    '''

    #Create a neural network model
    layers_list = [layers.Dense(parameters['hidden_layers'][0], activation='relu', input_shape=(X_train.shape[1],))]
    for i in range(1,len(parameters['hidden_layers'])):
        layers_list.append(layers.Dense(parameters['hidden_layers'][i], activation='relu'))
        layers_list.append(layers.Dropout(parameters['dropout'][i-1]))
    layers_list.append(layers.Dense(1))
    model = keras.Sequential(layers_list)

    # Compile the model
    model.compile(optimizer='adam', loss='mean_squared_error', metrics=['mae'])

    # Train the model
    if parameters['early_stopping']:
        early_stopping = keras.callbacks.EarlyStopping(monitor='val_loss', patience=10)
        model.fit(X_train, y_train, epochs=parameters['epochs'], batch_size=parameters['batch_size'], validation_split=parameters['validation_split'], callbacks=[early_stopping])
    else:
        model.fit(X_train, y_train, epochs=parameters['epochs'], batch_size=parameters['batch_size'], validation_split=parameters['validation_split'])

    # Evaluate the model
    loss, mae = model.evaluate(X_test, y_test)
    print(f"Model Mean Absolute Error: {mae}")
    return model




cursor_number_sets = load_database('counting_operations/outputs/ALL_POSSIBLE_4_Values_1.0_TO_20.0_GOAL_24.0.csv')
#print(create_reference_dictionary((1,100),4))
#mean absolute error 0.4 with 64,128,64 with binary outcome
#mean absolute error 0.38 with 64,1024,64 with binary outcome


# Call the function to train the model
model = train_model_random_forest(cursor_number_sets)
while True:
    user_input = input("Enter a list of integers separated by commas (or type 'exit' to quit): ")
    if user_input.lower() == 'exit':
        break
    try:
        input_values = sorted(list(map(int, user_input.split(','))))
        prediction = model.predict([input_values])
        print(f"Predicted outcome for {input_values}: {prediction[0]}")
        
        # Output the actual answer using the database
        cursor_number_sets.execute(f"""
            SELECT outcome 
            FROM number_sets 
            WHERE value_set_id = (
                SELECT id 
                FROM reference_dict 
                WHERE {' AND '.join([f'value{i+1} = {input_values[i]}' for i in range(len(input_values))])}
            )
        """)
        actual_outcome = cursor_number_sets.fetchone()
        if actual_outcome:
            print(f"Actual outcome for {input_values}: {actual_outcome[0]}")
        else:
            print(f"No actual outcome found for {input_values}.")
    except ValueError:
        print("Invalid input. Please enter integers only.")

# Output the prediction for a specific input
print(model.predict([[1,2,3,4]]))

cursor_number_sets.close()

