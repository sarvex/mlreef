import argparse
import sys
from keras import backend as K

class parameter(object):
    counter_call = 1

    def __init__(self, name, datatype, nullable, default):
        self.name = name
        self.datatype = datatype
        self.nullable = nullable
        self.default = default

    def __call__(self, f):
        def wrapped_function(*args):
            parser = argparse.ArgumentParser(description='Parser for Annotations')
            exec('parser.add_argument(\'{}\', action=\'store\', default=\'{}\', type={})'.format(self.name,
                                                                                                 self.default,
                                                                                                 self.datatype))

            params, __ = parser.parse_known_args(sys.argv[parameter.counter_call:])
            params = vars(params)
            exec('global {}; {} = params[\'{}\']'.format(self.name, self.name, self.name))
            parameter.counter_call += 1
            f(*args)
        return wrapped_function


# Parameter format: @parameter(name, datatype, nullable, default, description?)
class metric(object):

    def __init__(self, name):
        self.name = name

    def recall_m(self, y_pred):
        true_positives = K.sum(K.round(K.clip(self * y_pred, 0, 1)))
        possible_positives = K.sum(K.round(K.clip(self, 0, 1)))
        return true_positives / (possible_positives + K.epsilon())

    def precision_m(self, y_pred):
        true_positives = K.sum(K.round(K.clip(self * y_pred, 0, 1)))
        predicted_positives = K.sum(K.round(K.clip(y_pred, 0, 1)))
        return true_positives / (predicted_positives + K.epsilon())

    def f1_m(self, y_pred):
        precision = precision_m(self, y_pred)
        recall = recall_m(self, y_pred)
        return 2 * ((precision * recall) / (precision + recall + K.epsilon()))

    def __call__(self, f):
        def wrapped_function(*args):
            parser = argparse.ArgumentParser(description='Test1')
            exec('parser.add_argument(\'{}\', action=\'store\', default=\'{}\', type={})'.format(self.name))
            params, __ = parser.parse_known_args(sys.argv[parameter.counter_call:])
            params = vars(params)

            if self.name == 'recall':
                print("Recall Called")
            #exec('global {}; {} = params[\'{}\']'.format(self.name, self.name, self.name))
            parameter.counter_call += 1
            f(*args)
        return wrapped_function

# TODO FIXME: @V: Add dataOperation annotation please
@data_processor(
    name="Resnet 2.0 Filter",
    author="MLReef",
    type="ALGORITHM",
    description="Transforms images with lots of magic",
    visibility="PUBLIC",
    input_type="IMAGE",
    output_type="IMAGE"
)
@parameter("width", "Integer", False, 10)
@parameter("image_name", "String", False, 'SAR')
#@metric('recall')

def mmlreef():
    pass


mmlreef()

print(f"Width = {width} with type {type(width)}")
print(f"image_name = {image_name} with type {type(image_name)}")
