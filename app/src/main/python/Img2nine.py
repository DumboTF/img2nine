from PIL import Image
import time


def thum_img():
    im = Image.open("assets/abc.jpg")
    w, h = im.size
    print("Original image size: %s x %s" % (w, h))

    im.thumbnail((w // 2, h // 2))
    w2, h2 = im.size
    print("Resize image size: %s x %s" % (w2, h2))

    im.save("assets/abc_thumbnail.jpg", "jpeg")


def img_2_nine(img_path, des_folder):
    image = Image.open(img_path)
    new_img = fill_image(image)
    img_list = cut_image(new_img)
    names = img_path.split("/")
    simple_name = ""
    if names.__len__() > 1:
        name = names[names.__len__() - 1]
        simple_names = name.split(".")
        if simple_names.__len__() > 1:
            simple_name = simple_names[0]
    else:
        simple_name = str(time.time())
    save_images(img_list, des_folder, simple_name)


def fill_image(image):
    width, height = image.size
    len = width if width > height else height
    new_image = Image.new(image.mode, (len, len), color="white")
    if width > height:
        new_image.paste(image, (0, int((len - height) / 2)))
    else:
        new_image.paste(image, (int((len - width) / 2), 0))
    return new_image


def cut_image(image):
    width, height = image.size
    item_width = int(width / 3)
    box_list = []
    for i in range(3):
        for j in range(3):
            box = (j * item_width, i * item_width, (j + 1) * item_width, (i + 1) * item_width)
            box_list.append(box)
    image_list = [image.crop(box) for box in box_list]
    return image_list


def save_images(image_list, des_folder, name):
    index = 1
    for image in image_list:
        image.save(des_folder + "/" + name + "_" + str(index) + ".jpg", "jpeg")
        print("task "+str(index)+" ok")
        index += 1


if __name__ == '__main__':
    img_2_nine("assets/abc.jpg","assets")
