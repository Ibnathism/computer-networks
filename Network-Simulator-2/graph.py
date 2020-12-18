import matplotlib.pyplot as plt 
  

def plot(x, y, xlabel, ylabel, title):
    
    plt.scatter(x, y, color= "green",  marker= "o", s=30) 
    plt.plot(x, y) 

    plt.xlabel(xlabel) 
    plt.ylabel(ylabel) 
    plt.title(title) 
    
    # plt.legend() 
    
    plt.savefig(title+".jpg")


def test():
    x = [1, 2, 3, 4]
    y = [1, 4, 9, 16]

    plot(x=x, y=y, xlabel= "-X-", ylabel="-Y-", title="mein title")

test()  