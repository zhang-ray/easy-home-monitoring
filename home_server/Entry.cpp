#include "TcpServer.hpp"


void printUsage() {
    std::cerr << "Usage: EvcServer <port>\n";
}


int main(int argc, char* argv[]) {
    std::shared_ptr<Server> server_ = nullptr;
    try {
        int port = 6666;

        if ((argc != 1)&&(argc != 2)) {
            printUsage();
            return -1;
        }

        if (argc==2){
            port = std::atoi(argv[1]);
        }


        LOGI << "PORT=" << port;

        boost::log::core::get()->set_filter(boost::log::trivial::severity >= boost::log::trivial::trace);

        boost::asio::io_service io_service;

        server_ = std::make_shared<TcpServer>(io_service, port);


        io_service.run();
    }
    catch (std::exception& e) {
        std::cerr << "Exception: " << e.what() << "\n";
    }

    return 0;
}
