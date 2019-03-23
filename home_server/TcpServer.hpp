#pragma once
#include <deque>
#include <list>
#include <memory>
#include <array>
#include <set>
#include <utility>
#include <boost/asio.hpp>
#include <boost/bind.hpp>
#include <cstdlib>
#include <iostream>
#include <cstring>
#include "ReturnType.hpp"
#include "Logger.hpp"
#include <functional>
#include "Room.hpp"
#include "Server.hpp"

#include <fstream>




// one client-server connection
class TcpSession : public Participant, public std::enable_shared_from_this<TcpSession> {
public:
    TcpSession(boost::asio::ip::tcp::socket socket, Room& room)
        : socket_(std::move(socket))
        , room_(room) {
        BOOST_LOG_TRIVIAL(trace) << __FUNCTION__;
    }

    ~TcpSession() {
        BOOST_LOG_TRIVIAL(trace) << __FUNCTION__;
    }


    ReturnType start() {
        auto ret = room_.join(shared_from_this());
        if (!ret) {
            BOOST_LOG_TRIVIAL(error) << "Session start failed:" << ret.message();
            return ret;
        }

        BOOST_LOG_TRIVIAL(info) << "on accepted: " << socket_.remote_endpoint().address();

        ofs_.open("wtf.h264");
        readPayload();

        return 0;
    }



    virtual void deliver(const NetPacket& msg) override {
        bool write_in_progress = !write_msgs_.empty();
        write_msgs_.push_back(msg);
        if (!write_in_progress) {
            //write();
        }
    }

    virtual std::string info() override {
        auto __ = socket_.remote_endpoint();
        return __.address().to_string() + ":" + std::to_string(__.port());
    }

private:
    void readPayload() {
        auto self(shared_from_this());
        boost::asio::async_read(socket_, boost::asio::buffer(packet_.data(), packet_.size()),
            [this, self](boost::system::error_code ec, std::size_t length) {
            if (!ec) {

                {
                    BOOST_LOG_TRIVIAL(debug) << "Got packet_ @" << length;
                    ofs_.write(packet_.data(), length);
                }

                readPayload();
            }
            else {
                room_.leave(shared_from_this());
            }
        });
    }



private:
    boost::asio::ip::tcp::socket socket_;
    Room& room_;
    std::array<char, 1<<10> packet_;
    ClientPacketQueue write_msgs_;

    std::ofstream ofs_;
};




// one TCP server
class TcpServer :public Server{
public:
    TcpServer(boost::asio::io_service& io_service, int port)
        : acceptor_(io_service, boost::asio::ip::tcp::endpoint(boost::asio::ip::tcp::v4(), port))
        , socket_(io_service) {
        BOOST_LOG_TRIVIAL(info) << "Server started: port = " << port;
        doAccept();
    }

private:
    void doAccept() {
        acceptor_.async_accept(socket_, [this](boost::system::error_code errorCode) {
            if (!errorCode) {
                std::make_shared<TcpSession>(std::move(socket_), room_)->start();
            }

            //            LOGV << "async_accept new connection. socket_ = " << socket_;
            doAccept();
        });
    }

    boost::asio::ip::tcp::acceptor acceptor_;
    boost::asio::ip::tcp::socket socket_;
    Room room_;
};


