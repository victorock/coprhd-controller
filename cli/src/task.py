#!/usr/bin/python

#
# Copyright (c) 2016 EMC Corporation
# All Rights Reserved
#
# This software contains the intellectual property of EMC Corporation
# or is licensed to EMC Corporation from third parties.  Use of this
# software and the intellectual property contained therein is expressly
# limited to the terms and conditions of the License Agreement under which
# it is provided by or on behalf of EMC.
#

import common
from threading import Timer
from common import SOSError
import time
from urihelper import singletonURIHelperInstance


'''
The class definition for the Task operations
'''


class Task(object):

    COMPONENT_TYPE = "task"

    isTimeout = False
    timeout = 300    # seconds

    def __init__(self, ipAddr, port):
        '''
            Constructor: takes IP address and port of the ViPR instance.
            These are needed to make http requests for REST API
            '''
        self.__ipAddr = ipAddr
        self.__port = port

    '''
        Get the root of the catalog
        '''

    def resume_task_by_uri(self, uri):
        command = singletonURIHelperInstance.getUri(
            self.COMPONENT_TYPE,
            "resume")
        (s, h) = common.service_json_request(self.__ipAddr, self.__port, "POST",
                                             command.format(uri),
                                             None)


'''
Preprocessor for the task resume operation
'''


def task_resume(args):
    taskObj = Task(args.ip, args.port)
    try:
        taskObj.resume_task_by_uri(args.uri)
        return
    except SOSError as e:
        common.format_err_msg_and_raise(
            "resume",
            "task",
            e.err_text,
            e.err_code)

    return


# resume command parser

def resume_parser(subcommand_parsers, common_parser):
    resume_parser = subcommand_parsers.add_parser(
        'resume',
        description='ViPR Task resume CLI usage',
        parents=[common_parser],
        conflict_handler='resolve',
        help='Resume a suspended task')

    mandatory_args = resume_parser.add_argument_group('mandatory arguments')
    mandatory_args.add_argument('-id', '-uri',
                                metavar='<uri>',
                                dest='uri',
                                help='URI of the Task',
                                required=True)

    resume_parser.set_defaults(func=task_resume)

#
# Tasks Main parser routine
#

def task_parser(parent_subparser, common_parser):

    parser = parent_subparser.add_parser('task',
                                         description='ViPR task CLI usage',
                                         parents=[common_parser],
                                         conflict_handler='resolve',
                                         help='Operations on Task')
    subcommand_parsers = parser.add_subparsers(help='use one of sub-commands')

    # resume parser
    resume_parser(subcommand_parsers, common_parser)
