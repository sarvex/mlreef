from annotations.parameter_annotations import parameter
import annotations.parameter_annotations as params


@parameter(name="width", datatype="int", required=False, defaultValue=100)
@parameter(name="height", datatype="int", required=False, defaultValue=200)
@parameter(name="image_name", datatype="str", required=False, defaultValue='SAR')
def inject_variables():
    print("Variables have been injected into the scope of this file")


if __name__ == '__main__':
    inject_variables()
    print(f"Width = {params.width} with type {type(params.width)}")
    print(f"Height = {params.height} with type {type(params.height)}")
    print(f"Image Name = {params.image_name} with type {type(params.image_name)}")
